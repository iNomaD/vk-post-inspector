import com.vk.api.sdk.objects.users.UserXtrCounters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denis on 02.01.2017.
 */
public class Filter {
    VkParser vkParser;
    Boolean isWoman;
    Integer cityId;
    Integer minAge;
    Integer maxAge;

    public Filter(VkParser vkParser, Boolean isWoman, Integer cityId, Integer minAge, Integer maxAge){
        this.vkParser = vkParser;
        this.isWoman = isWoman;
        this.cityId = cityId;
        this.minAge = minAge;
        this.maxAge = maxAge;
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
        else if(minAge != null && maxAge != null){
            return false;
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
