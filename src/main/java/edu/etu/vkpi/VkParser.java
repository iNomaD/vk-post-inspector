package edu.etu.vkpi;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.likes.responses.GetListResponse;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.users.responses.SearchResponse;
import com.vk.api.sdk.objects.wall.WallComment;
import com.vk.api.sdk.objects.wall.responses.GetCommentsResponse;
import com.vk.api.sdk.queries.likes.LikesGetListFilter;
import com.vk.api.sdk.queries.likes.LikesType;
import com.vk.api.sdk.queries.users.UserField;
import com.vk.api.sdk.queries.users.UsersSearchQuery;
import com.vk.api.sdk.queries.users.UsersSearchSex;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class VkParser {
    private static final int STEP = 1000;
    private static final int USERS = 1000;
    private static final int COMMENTS = 100;

    private static final int TIMEOUT = 100;

    VkApiClient vk;
    private UserActor userActor;

    public VkParser(VkApiClient vk){
        this.vk = vk;
    };

    public VkParser(VkApiClient vk, UserActor user){
        this(vk);
        this.userActor = user;
    }

    public List<Integer> getUsersLiked(int ownerId, int itemId) throws ApiException, ClientException, InterruptedException{
        int offset = 0;
        List<Integer> result = new LinkedList<Integer>();
        GetListResponse usersLiked = vk.likes().getList(userActor, LikesType.POST).ownerId(ownerId).itemId(itemId).offset(offset).count(STEP).execute();
        result.addAll(usersLiked.getItems());
        Integer count = usersLiked.getCount();

        while(result.size() < count && offset < count){
            offset += STEP;
            try {
                usersLiked = vk.likes().getList(userActor, LikesType.POST).ownerId(ownerId).itemId(itemId).offset(offset).count(STEP).execute();
                result.addAll(usersLiked.getItems());
                Thread.sleep(TIMEOUT/10);
                System.out.println("l_"+result.size()+"/"+count);
            }
            catch (ApiTooManyException e){
                offset -= STEP;
                Thread.sleep(TIMEOUT*10);
                System.out.println("Retrying get likes ("+offset+"/"+count+")");
            }
        }
        return result;
    }

    public List<Integer> getUsersReposted(int ownerId, int itemId) throws ApiException, ClientException, InterruptedException{
        int offset = 0;
        List<Integer> result = new LinkedList<Integer>();
        GetListResponse usersReposted = vk.likes().getList(userActor, LikesType.POST).ownerId(ownerId).itemId(itemId).offset(offset).count(STEP).filter(LikesGetListFilter.COPIES).execute();
        result.addAll(usersReposted.getItems());
        Integer count = usersReposted.getCount();

        while(result.size() < count && offset < count){
            offset += STEP;
            try {
                usersReposted = vk.likes().getList(userActor, LikesType.POST).ownerId(ownerId).itemId(itemId).offset(offset).count(STEP).filter(LikesGetListFilter.COPIES).execute();
                result.addAll(usersReposted.getItems());
                Thread.sleep(TIMEOUT/10);
                System.out.println("r_"+result.size()+"/"+count);
            }
            catch (ApiTooManyException e){
                offset -= STEP;
                Thread.sleep(TIMEOUT*10);
                System.out.println("Retrying get reposts ("+offset+"/"+count+")");
            }
        }

        return result;
    }

    public Map<Integer, String> getUsersCommented(int ownerId, int itemId) throws ApiException, ClientException, InterruptedException{
        int offset = 0;
        List<WallComment> comments = new LinkedList<>();
        GetCommentsResponse commentsResponse = vk.wall().getComments(userActor, itemId).ownerId(ownerId).offset(offset).count(COMMENTS).execute();
        comments.addAll(commentsResponse.getItems());
        Integer count = commentsResponse.getCount();

        while(comments.size() < count && offset < count){
            offset += COMMENTS;
            try {
                commentsResponse = vk.wall().getComments(userActor, itemId).ownerId(ownerId).offset(offset).count(COMMENTS).execute();
                comments.addAll(commentsResponse.getItems());
                Thread.sleep(TIMEOUT/10);
                System.out.println("c_"+comments.size()+"/"+count);
            }
            catch (ApiTooManyException e){
                offset -= COMMENTS;
                Thread.sleep(TIMEOUT*10);
                System.out.println("Retrying get comments ("+offset+"/"+count+")");
            }
        }

        Map<Integer, String> userComments = new HashMap<>();
        for(WallComment wc : comments){
            if(userComments.containsKey(wc.getFromId())){
                String oldValue = userComments.get(wc.getFromId());
                userComments.put(wc.getFromId(), oldValue.concat(";| ").concat(wc.getText()));
            }
            userComments.put(wc.getFromId(), wc.getText());
        }
        return userComments;
    }

    public List<UserXtrCounters> getUserProfiles(Collection<Integer> ids) throws ApiException, ClientException, InterruptedException{
        List<UserXtrCounters> result = new LinkedList<>();
        List<String> userIds = new LinkedList<>();
        int counter = 0;
        for(Integer item : ids){
            userIds.add(item.toString());
            ++counter;
            if(counter % USERS == 0 || counter == ids.size()){
                while(true) {
                    try {
                        List<UserXtrCounters> response = vk.users().get(userActor).userIds(userIds).fields(UserField.SEX, UserField.BDATE, UserField.CITY, UserField.RELATION, UserField.DOMAIN).execute();
                        result.addAll(response);
                        userIds.clear();
                        System.out.println("u_"+counter+"/"+ids.size());
                        Thread.sleep(TIMEOUT/10);
                        break;
                    }
                    catch (ApiTooManyException e){
                        Thread.sleep(TIMEOUT*10);
                        System.out.println("Retrying get profiles ("+counter+"/"+ids.size()+")");
                    }
                }
            }
        }
        return result;
    }

    public Pair<List<UserFull>, Integer> parseUsersByParameters(Integer groupId, Integer cityId, Integer minAge, Integer maxAge, Boolean isWoman, int count, Integer month, Integer day) throws ApiException, ClientException, InterruptedException{
        UsersSearchQuery query = vk.users().search(userActor).groupId(groupId).count(count);
        if(cityId != null){
            query = query.city(cityId);
        }
        if(minAge != null){
            query = query.ageFrom(minAge);
        }
        if(maxAge != null){
            query = query.ageTo(maxAge);
        }
        if(isWoman != null){
            query = query.sex(isWoman? UsersSearchSex.FEMALE : UsersSearchSex.MALE);
        }
        if(month != null){
            query = query.birthMonth(month);
        }
        if(day != null){
            query = query.birthDay(day);
        }
        while (true) {
            try {
                SearchResponse searchResponse = query.execute();
                Thread.sleep(TIMEOUT);
                return new Pair<List<UserFull>, Integer>() {
                    @Override
                    public List<UserFull> getLeft() {
                        return searchResponse.getItems();
                    }

                    @Override
                    public Integer getRight() {
                        return searchResponse.getCount();
                    }

                    @Override
                    public Integer setValue(Integer value) {
                        return searchResponse.getCount();
                    }
                };
            } catch (ApiTooManyException e) {
                Thread.sleep(TIMEOUT * 10);
                System.out.println("Retrying parseUsersByParameters");
            }
        }
    }

    /*private List<UserFull> searchUsersByGroup(String firstName, String lastName, Integer city, Integer minAge, Integer maxAge) throws ApiException, ClientException, InterruptedException{
        UsersSearchQuery query = vk.users().search(userActor).q(firstName+" "+lastName).groupId(-edu.etu.vkpi.Config.ownerId).city(city).count(10);
        if(minAge != null){
            query = query.ageFrom(minAge);
        }
        if(maxAge != null){
            query = query.ageTo(maxAge);
        }
        while (true) {
            try {
                SearchResponse searchResponse = query.execute();
                Thread.sleep(TIMEOUT);
                return searchResponse.getItems();
            } catch (ApiTooManyException e) {
                Thread.sleep(TIMEOUT * 10);
                System.out.println("Retrying get age by search");
            }
        }
    }

    public boolean checkUserByGroup(Integer id, String firstName, String lastName, Integer city, Integer minAge, Integer maxAge) throws ApiException, ClientException, InterruptedException{
        List<UserFull> users = searchUsersByGroup(firstName, lastName, city, minAge, maxAge);
        for(UserFull u : users){
            if(u.getId().equals(id)){
                return true;
            }
        }
        return false;
    }*/
}
