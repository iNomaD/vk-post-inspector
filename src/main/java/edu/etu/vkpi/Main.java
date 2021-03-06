package edu.etu.vkpi;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.polls.Answer;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args){
        boolean getToken = false;
        for(String argument : args){
            if(argument.equals("-token")){
                getToken = true;
            }
        }

        if(!Config.loadProperties()){
            System.out.println("Can't load config from "+Config.CONF_FILE_NAME+" !");
            return;
        }

        if(getToken){
            Auth.getToken();
            return;
        }

        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor userActor = Auth.authorize(vk, Config.userId, Config.accessToken);
        System.out.println(userActor);

        VkParser vkParser = new VkParser(vk, userActor);
        Filter filter = new Filter(vkParser, Config.isWoman, Config.cityId, Config.minAge, Config.maxAge);
        try {
            if(Config.advancedAge){
                filter.findAppropriateUsers();
            }

            List<Integer> usersLikedIds = vkParser.getUsersLiked(Config.ownerId, Config.itemId);
            usersLikedIds = usersLikedIds.stream().filter(p -> p > 0).collect(Collectors.toList());
            System.out.println("Filtering users...");
            List<SimpleUser> usersLiked = filter.filterCollection(usersLikedIds);

            List<Integer> usersRepostedIds = vkParser.getUsersReposted(Config.ownerId, Config.itemId);
            usersRepostedIds = usersRepostedIds.stream().filter(p -> p > 0).collect(Collectors.toList());
            System.out.println("Filtering users...");
            List<SimpleUser> usersReposted = filter.filterCollection(usersRepostedIds);

            Map<Integer, String> userComments = vkParser.getUsersCommented(Config.ownerId, Config.itemId);
            Set<Integer> usersCommentedIds = userComments.keySet();
            usersCommentedIds = usersCommentedIds.stream().filter(p -> p > 0).collect(Collectors.toSet());
            System.out.println("Filtering users...");
            List<SimpleUser> usersCommented = filter.filterCollection(usersCommentedIds);

            Map<Answer, List<Integer>> usersVoted = vkParser.getUsersInPoll(Config.ownerId, Config.itemId);
            List<Integer> usersVotedIds = new ArrayList<>();
            usersVoted.values().forEach(usersVotedIds::addAll);
            usersVotedIds = usersVotedIds.stream().filter(p -> p > 0).collect(Collectors.toList());
            System.out.println("Filtering users...");
            List<SimpleUser> usersVotedModels = filter.filterCollection(usersVotedIds);
            Map<Integer, SimpleUser> usersVotedModelsMap = new HashMap<>();
            usersVotedModels.forEach(user -> usersVotedModelsMap.put(user.getId(), user));

            String outname = Config.outputName;
            PrintStream standard = System.out;
            if(Config.outputToFile) {
                PrintStream st = new PrintStream(new FileOutputStream(outname));
                System.setOut(st);
            }

            System.out.println("Wall https://vk.com/wall"+Config.ownerId+"_"+Config.itemId);

            System.out.println("Likes before filter: "+usersLikedIds.size());
            System.out.println("Likes after filter: "+usersLiked.size());
            for(SimpleUser user : usersLiked){
                System.out.println(user.format());
            }

            System.out.println("Reposts before filter: "+usersRepostedIds.size());
            System.out.println("Reposts after filter: "+usersReposted.size());
            for(SimpleUser user : usersReposted){
                System.out.println(user.format());
            }

            System.out.println("Commentors before filter: "+usersCommentedIds.size());
            System.out.println("Commentors after filter: "+usersCommented.size());
            for(SimpleUser user : usersCommented){
                System.out.println(user.format() + " " + userComments.get(user.getId()));
            }

            for(Map.Entry<Answer, List<Integer>> entry : usersVoted.entrySet()){
                System.out.println("Poll entry \""+entry.getKey().getText()+"\"");
                System.out.println("Users before filter "+entry.getValue().size());
                List<Integer> filteredValue = entry.getValue().stream().filter(usersVotedModelsMap::containsKey).collect(Collectors.toList());
                System.out.println("Users after filter "+filteredValue.size());
                for(Integer userId : filteredValue){
                    SimpleUser user = usersVotedModelsMap.get(userId);
                    System.out.println(user.format());
                }
            }

            if(Config.outputToFile){
                System.setOut(standard);
                System.out.println("Program finished. Results in "+outname);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
