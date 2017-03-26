package ldap.sample;

import ldap.sample.domain.User;
import org.junit.*;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;

public class UserTest {
	
	@Mock
	UserRepo userRepo;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		User user = new User();
		user.setDayOfBirth("12/12/2012");
		user.setEmail("agt1@ipos.com");
		user.setFullName("Hoang Nguyen");
		user.setIdNumber("12345678");
		user.setIsActive("Y");
		user.addPhoneNumber("+84 7236487263");
		user.addPhoneNumber("+08 7236487263");
		user.addAddress("Etown 3");
		user.addAddress("Etown 5");
		user.addApproverId("agt2@ipos.com");
		user.addApproverId("agt3@ipos.com");
		user.setUserName("agt1@ipos.com");
		user.setUserPassword("C3K4H3U43U4HH4CN3476C2327R897QY283YX2NH30872Y=-");
		
		userRepo.save(user);
	}

}
