import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Denis on 13.11.2016.
 */
public class Main {
    public static void main(String[] args){
        if(!Config.loadProperties()){
            System.out.println("Can't load config from "+Config.CONF_FILE_NAME+" !");
            return;
        }

        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor userActor = Auth.authorize(vk, Config.userId, Config.accessToken);
        System.out.println(userActor);

        VkParser vkParser = new VkParser(vk, userActor);
        Filter filter = new Filter(vkParser, Config.isWoman, Config.cityId, Config.minAge, Config.maxAge);
        try {
            List<Integer> usersLikedIds = vkParser.getUsersLiked(Config.ownerId, Config.itemId);
            usersLikedIds = usersLikedIds.stream().filter(p -> p > 0).collect(Collectors.toList());
            List<SimpleUser> usersLiked = filter.filterList(usersLikedIds);

            List<Integer> usersRepostedIds = vkParser.getUsersReposted(Config.ownerId, Config.itemId);
            usersRepostedIds = usersRepostedIds.stream().filter(p -> p > 0).collect(Collectors.toList());
            List<SimpleUser> usersReposted = filter.filterList(usersRepostedIds);


            Map<Integer, String> userComments = vkParser.getUsersCommented(Config.ownerId, Config.itemId);
            Set<Integer> usersCommentedIds = userComments.keySet();
            usersCommentedIds = usersCommentedIds.stream().filter(p -> p > 0).collect(Collectors.toSet());
            List<SimpleUser> usersCommented = filter.filterList(usersCommentedIds);

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

            System.out.println("Commentor before filter: "+usersCommentedIds.size());
            System.out.println("Commentors after filter: "+usersCommented.size());
            for(SimpleUser user : usersCommented){
                System.out.println(user.format() + " " + userComments.get(user.getId()));
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
