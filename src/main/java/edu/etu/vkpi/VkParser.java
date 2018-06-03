package edu.etu.vkpi;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.likes.responses.GetListResponse;
import com.vk.api.sdk.objects.polls.Answer;
import com.vk.api.sdk.objects.polls.Poll;
import com.vk.api.sdk.objects.polls.Voters;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.users.responses.SearchResponse;
import com.vk.api.sdk.objects.wall.WallComment;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.responses.GetCommentsResponse;
import com.vk.api.sdk.queries.likes.LikesGetListFilter;
import com.vk.api.sdk.queries.likes.LikesType;
import com.vk.api.sdk.queries.users.UserField;
import com.vk.api.sdk.queries.users.UsersSearchQuery;
import com.vk.api.sdk.queries.users.UsersSearchSex;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class VkParser {
    private static final int LIKES_STEP = 1000;
    private static final int COMMENTS_STEP = 100;
    private static final int USERS_GET_LIMIT = 1000;

    private VkApiClient vk;
    private UserActor userActor;

    public VkParser(VkApiClient vk, UserActor user){
        this.vk = vk;
        this.userActor = user;
    }

    public List<Integer> getUsersLiked(int ownerId, int itemId) throws ApiException, ClientException, InterruptedException{
        int count = Integer.MAX_VALUE;
        int offset = 0;
        List<Integer> result = new LinkedList<>();

        while(result.size() < count && offset < count){
            try {
                GetListResponse usersLiked = new QueryWithDelay<>(vk.likes().getList(userActor, LikesType.POST).ownerId(ownerId).itemId(itemId).offset(offset).count(LIKES_STEP), Config.timeout).execute();
                count = usersLiked.getCount();
                result.addAll(usersLiked.getItems());
                offset += LIKES_STEP;
                System.out.println("likes_"+result.size()+"/"+count);
            }
            catch (ApiTooManyException e){
                Thread.sleep(Config.timeout);
                System.out.println("Retrying get likes ("+offset+"/"+count+")");
            }
        }
        return result;
    }

    public List<Integer> getUsersReposted(int ownerId, int itemId) throws ApiException, ClientException, InterruptedException{
        int count = Integer.MAX_VALUE;
        int offset = 0;
        List<Integer> result = new LinkedList<>();

        while(result.size() < count && offset < count){
            try {
                GetListResponse usersReposted = new QueryWithDelay<>(vk.likes().getList(userActor, LikesType.POST).ownerId(ownerId).itemId(itemId).offset(offset).count(LIKES_STEP).filter(LikesGetListFilter.COPIES), Config.timeout).execute();
                count = usersReposted.getCount();
                result.addAll(usersReposted.getItems());
                offset += LIKES_STEP;
                System.out.println("reposts_"+result.size()+"/"+count);
            }
            catch (ApiTooManyException e){
                Thread.sleep(Config.timeout);
                System.out.println("Retrying get reposts ("+offset+"/"+count+")");
            }
        }

        return result;
    }

    public Map<Integer, String> getUsersCommented(int ownerId, int itemId) throws ApiException, ClientException, InterruptedException{
        int count = Integer.MAX_VALUE;
        int offset = 0;
        List<WallComment> comments = new LinkedList<>();

        while(comments.size() < count && offset < count){
            try {
                GetCommentsResponse commentsResponse = new QueryWithDelay<>(vk.wall().getComments(userActor, itemId).ownerId(ownerId).offset(offset).count(COMMENTS_STEP), Config.timeout).execute();
                count = commentsResponse.getCount();
                comments.addAll(commentsResponse.getItems());
                offset += COMMENTS_STEP;
                System.out.println("c_"+comments.size()+"/"+count);
            }
            catch (ApiTooManyException e){
                Thread.sleep(Config.timeout);
                System.out.println("Retrying get comments ("+offset+"/"+count+")");
            }
        }

        Map<Integer, String> userComments = new HashMap<>();
        for(WallComment wc : comments){
            String link = "https://vk.com/wall"+Config.ownerId+"_"+Config.itemId+"?reply="+wc.getId();
            String comment = "[" + link + "]='"+wc.getText()+"'";
            if(userComments.containsKey(wc.getFromId())){
                String oldValue = userComments.get(wc.getFromId());
                userComments.put(wc.getFromId(), oldValue.concat(";| ").concat(comment));
            }
            else{
                userComments.put(wc.getFromId(), comment);
            }
        }
        return userComments;
    }

    public Map<Answer, List<Integer>> getUsersInPoll(int ownerId, int itemId) throws ApiException, ClientException, InterruptedException{
        Map<Answer, List<Integer>> map = new HashMap<>();;
        List<WallPostFull> list = new QueryWithDelay<>(vk.wall().getById(userActor, ownerId+"_"+itemId), Config.timeout).execute();
        List<WallpostAttachment> wallpostAttachments = !(list == null || list.isEmpty()) ? list.get(0).getAttachments() : new ArrayList<>();
        for(WallpostAttachment attachment : wallpostAttachments){
            if(attachment.getType() == WallpostAttachmentType.POLL){
                Poll poll = attachment.getPoll();
                List<Answer> answers = poll.getAnswers();
                List<Integer> answerIds = answers.stream().map(Answer::getId).collect(Collectors.toList());
                for(Answer answer : answers){
                    map.put(answer, new ArrayList<>());
                }

                int count = Integer.valueOf(poll.getVotes());
                int offset = 0;
                int votersSize = 0;
                while(votersSize < count){
                    try {
                        List<Voters> voters_list = new QueryWithDelay<>(vk.polls().getVoters(userActor, poll.getId(), answerIds).ownerId(ownerId).offset(offset).count(LIKES_STEP), Config.timeout).execute();
                        for(Voters voters : voters_list){
                            for(Map.Entry<Answer, List<Integer>> entry : map.entrySet()){
                                if(entry.getKey().getId().equals(voters.getAnswerId())){
                                    entry.getValue().addAll(voters.getUsers().getItems());
                                    votersSize += voters.getUsers().getItems().size();
                                }
                            }
                        }
                        offset += LIKES_STEP;
                        System.out.println("v_"+votersSize+"/"+count);
                    }
                    catch (ApiTooManyException e){
                        Thread.sleep(Config.timeout);
                        System.out.println("Retrying get voters ("+votersSize+"/"+count+")");
                    }
                }
                break;
            }
        }
        return map;
    }

    public List<UserXtrCounters> getUserProfiles(Collection<Integer> ids) throws ApiException, ClientException, InterruptedException{
        List<UserXtrCounters> result = new LinkedList<>();
        List<String> userIds = new LinkedList<>();
        int counter = 0;
        for(Integer item : ids){
            userIds.add(item.toString());
            ++counter;
            if(counter % USERS_GET_LIMIT == 0 || counter == ids.size()){
                while(true) {
                    try {
                        List<UserXtrCounters> response = new QueryWithDelay<>(vk.users().get(userActor).userIds(userIds).fields(UserField.SEX, UserField.BDATE, UserField.CITY, UserField.RELATION, UserField.DOMAIN), Config.timeout).execute();
                        result.addAll(response);
                        userIds.clear();
                        System.out.println("users_"+counter+"/"+ids.size());
                        break;
                    }
                    catch (ApiTooManyException e){
                        Thread.sleep(Config.timeout);
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
                SearchResponse searchResponse = new QueryWithDelay<>(query, Config.timeout).execute();
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
                Thread.sleep(Config.timeout);
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
