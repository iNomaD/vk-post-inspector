import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Denis on 02.01.2017.
 */
public class Filter {
    VkParser vkParser;
    Boolean isWoman;
    Integer cityId;
    Integer minAge;
    Integer maxAge;
    Set<Integer> appropriateUsers = new HashSet<>();

    public Filter(VkParser vkParser, Boolean isWoman, Integer cityId, Integer minAge, Integer maxAge){
        this.vkParser = vkParser;
        this.isWoman = isWoman;
        this.cityId = cityId;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public void findAppropriateUsers(){
        try {
            System.out.println("Appropriate users: "+"https://api.vk.com/method/users.search?group_id="+(-Config.ownerId)+"&city="+cityId+"&sex="+(isWoman ? 1 : 2)+"&age_from="+minAge+"&age_to="+maxAge+"&count=10&access_token="+Config.accessToken+"&v=5.60");
            Pair<List<UserFull>, Integer> all = vkParser.parseUsersByParameters(-Config.ownerId, cityId, minAge, maxAge, isWoman, 1000, null, null);
            appropriateUsers.addAll(all.getLeft().stream().map(user -> user.getId()).collect(Collectors.toSet()));
            if(all.getRight() > 1000 ){
                for (int month=1; month<=12; ++month){
                    Pair<List<UserFull>, Integer> part = vkParser.parseUsersByParameters(-Config.ownerId, cityId, minAge, maxAge, isWoman, 1000, month, null);
                    appropriateUsers.addAll(part.getLeft().stream().map(user -> user.getId()).collect(Collectors.toSet()));
                }
            }
            System.out.println("Found "+appropriateUsers.size()+"/"+all.getRight()+" appropriate users in group");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean filterSex(SimpleUser user){
        int sex = user.getSex();
        if(isWoman != null){
            if(isWoman == true && sex == 2){
                return false;
            }
            if(isWoman == false && sex == 1){
                return false;
            }
        }
        return true;
    }

    private boolean filterCity(SimpleUser user){
        if(cityId != null && user.getCityId() != cityId){
            return false;
        }
        return true;
    }

    public boolean filterAge(SimpleUser user){
        if(user.getAge() != null){
            if(minAge != null && user.getAge() < minAge || maxAge != null && user.getAge() > maxAge){
                return false;
            }
        }
        /*
        else if(minAge != null && maxAge != null){
            return false;
        }*/
        return true;
    }

    public boolean filterAgeAdvanced(SimpleUser user){
        try {
            return appropriateUsers.contains(user.getId());
            //return vkParser.checkUserByGroup(user.getId(), user.getFirstName(), user.getLastName(), user.getCityId(), minAge, maxAge);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public List<SimpleUser> filterList(Collection<Integer> ids){
        List<SimpleUser> result = new LinkedList<>();
        try {
            List<UserXtrCounters> userXtrCounterses = vkParser.getUserProfiles(ids);
            for(UserXtrCounters item : userXtrCounterses){
                SimpleUser user = new SimpleUser();
                user.setId(item.getId());
                user.setFirstName(item.getFirstName());
                user.setLastName(item.getLastName());
                user.setBdate(item.getBdate());
                user.setRelation(item.getRelation());
                user.setCityId(item.getCity() == null ? null : item.getCity().getId());
                user.setCityTitle(item.getCity() == null ? null : item.getCity().getTitle());
                user.setDomain(item.getDomain());
                user.setSex(item.getSex() == null ? null : item.getSex().getValue());
                user.calculateAge();
                if(filterSex(user) && filterCity(user) && filterAge(user)){
                    if(Config.advancedAge && user.getAge() == null){
                        if(!filterAgeAdvanced(user)) {
                            continue;
                        }
                    }
                    result.add(user);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
