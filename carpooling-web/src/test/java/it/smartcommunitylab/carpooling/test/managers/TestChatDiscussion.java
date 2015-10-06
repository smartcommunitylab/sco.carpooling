package it.smartcommunitylab.carpooling.test.managers;

import it.smartcommunitylab.carpooling.managers.CarPoolingManager;
import it.smartcommunitylab.carpooling.model.Community;
import it.smartcommunitylab.carpooling.model.Discussion;
import it.smartcommunitylab.carpooling.model.Message;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.User;
import it.smartcommunitylab.carpooling.mongo.repos.CommunityRepository;
import it.smartcommunitylab.carpooling.mongo.repos.DiscussionRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRequestRepository;
import it.smartcommunitylab.carpooling.mongo.repos.UserRepository;
import it.smartcommunitylab.carpooling.test.TestConfig;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })

public class TestChatDiscussion {

	@Autowired
	private CarPoolingManager carPoolingManager;
	@Autowired
	private TravelRequestRepository travelRequestRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private TravelRepository travelRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private DiscussionRepository discussionRepository;
	
	private ObjectMapper mapper = new ObjectMapper();

	@After
	public void after() {
		travelRequestRepository.deleteAll();
		communityRepository.deleteAll();
		travelRepository.deleteAll();
		userRepository.deleteAll();
		discussionRepository.deleteAll();

	}

	@Before
	public void before() {

		travelRequestRepository.deleteAll();
		communityRepository.deleteAll();
		travelRepository.deleteAll();
		userRepository.deleteAll();
		discussionRepository.deleteAll();

		InputStream travelJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("travel.json");
		InputStream userJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("users.json");
		InputStream communityJson = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("community.json");
		try {
			JsonNode travelRootNode = mapper.readTree(travelJson);
			ArrayNode travelArrayNode = (ArrayNode) travelRootNode;

			for (JsonNode node : travelArrayNode) {
				Travel refTravel = mapper.convertValue(node, Travel.class);
				travelRepository.save(refTravel);

			}

			JsonNode userRootNode = mapper.readTree(userJson);
			ArrayNode usersArrayNode = (ArrayNode) userRootNode;

			for (JsonNode uNode : usersArrayNode) {
				User refUser = mapper.convertValue(uNode, User.class);
				userRepository.save(refUser);

			}

			JsonNode communityRootNode = mapper.readTree(communityJson);
			ArrayNode commArrayNode = (ArrayNode) communityRootNode;

			for (JsonNode cNode : commArrayNode) {
				Community comm = mapper.convertValue(cNode, Community.class);
				communityRepository.save(comm);
			}

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testPassengerDriverDiscussion() {

		/**
		 * 1. User("52") send Msg1 to driver ("54").
		 * 2. Driver("54") send reply.
		 * 3. User("52") send Msg2 to driver ("54").
		 * 4. Driver(:54") send reply.
		 * 5. User("53") send Msg1 to driver("54").
		 * 6. User("52") read thread with Driver, in total 4 msgs.
		 * 7. User("53") read thread with Driver, in total 1 msg.
		 * 8. Driver("54") read thread with User("52"), in total 4 msgs.
		 * 9. Driver("54") read thread with User("53"), in total 1 msg.
		 * 10. Driver("54") reply to User("53") and read thread again, in total 2 msgs.
		 */
		
		Message msg1 = new Message("52", System.currentTimeMillis(), "Ciao, quanti posti disponible", "54");
		carPoolingManager.sendMessage("52", "560263eed1f1f802c2a83efg", msg1);

		Message repl1 = new Message("54", System.currentTimeMillis(), "2 posti disponible", "52");
		carPoolingManager.sendMessage("54", "560263eed1f1f802c2a83efg", repl1);

		Message msg2 = new Message("52", System.currentTimeMillis(), "sono a statzione di Trento", "54");
		carPoolingManager.sendMessage("52", "560263eed1f1f802c2a83efg", msg2);

		Message repl2 = new Message("54", System.currentTimeMillis(), "vabene, arrivo in 5 minuti", "52");
		carPoolingManager.sendMessage("54", "560263eed1f1f802c2a83efg", repl2);

		Message msg3 = new Message("53", System.currentTimeMillis(), "Ciao, hai uno posto", "54");
		carPoolingManager.sendMessage("53", "560263eed1f1f802c2a83efg", msg3);

		Discussion discussionUser1 = carPoolingManager.readDiscussion("52", "560263eed1f1f802c2a83efg", "54");

		Assert.assertEquals(discussionUser1.getMessages().size(), 4);

		Discussion discussionUser2 = carPoolingManager.readDiscussion("53", "560263eed1f1f802c2a83efg", "54");

		Assert.assertEquals(discussionUser2.getMessages().size(), 1);

		Discussion discussionDriver1 = carPoolingManager.readDiscussion("54", "560263eed1f1f802c2a83efg", "52");

		Assert.assertEquals(discussionDriver1.getMessages().size(), discussionUser1.getMessages().size());

		Discussion discussionDriver2 = carPoolingManager.readDiscussion("54", "560263eed1f1f802c2a83efg", "53");

		Assert.assertEquals(discussionDriver2.getMessages().size(), discussionUser2.getMessages().size());

		Message replUser2 = new Message("54", System.currentTimeMillis(), "Si disponible", "53");
		carPoolingManager.sendMessage("54", "560263eed1f1f802c2a83efg", replUser2);

		discussionDriver2 = carPoolingManager.readDiscussion("54", "560263eed1f1f802c2a83efg", "53");

		Assert.assertEquals(discussionDriver2.getMessages().size(), 2);		
		
	}

	@Test
	public void testPassengerPassengerDiscussion() {

		/**
		 * 1. User("52") send msg to passenger ("53")
		 * 2. Passenger("53") reply with no thanks.
		 * 3. User("52") close thread with compliments.
		 * 5. User("52") read thread with passenger("53")
		 * 6. Passenger("53") read thread with User("52"), equal no.of msgs.
		 */
		
		Message msg1 = new Message("52", System.currentTimeMillis(), "Ciao, driver 54 hai 1 posto disponible, vuoi venire", "53");
		carPoolingManager.sendMessage("52", "560263eed1f1f802c2a83efg", msg1);

		Message repl1 = new Message("53", System.currentTimeMillis(), "Grazie, Ho prenotato con treno, Buon Viaggio!!", "52");
		carPoolingManager.sendMessage("53", "560263eed1f1f802c2a83efg", repl1);
		
		Message ack1 = new Message("52", System.currentTimeMillis(), "Buon Viaggio!!, anche te", "53");
		carPoolingManager.sendMessage("52", "560263eed1f1f802c2a83efg", ack1);
		
		Discussion discussionU1 = carPoolingManager.readDiscussion("52", "560263eed1f1f802c2a83efg", "53");
		
		Discussion discussionU2 = carPoolingManager.readDiscussion("53", "560263eed1f1f802c2a83efg", "52");
		
		Assert.assertEquals(discussionU1.getMessages().size(), discussionU2.getMessages().size());
		
	}

}