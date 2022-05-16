package eu.woolplatform.variableservice.service;

import eu.woolplatform.variableservice.model.User;
import eu.woolplatform.variableservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
@Transactional
public class UserService {
	@Autowired
	private UserRepository userRepository;
	public List<User> listAllUser() {
		return userRepository.findAll();
	}

	public void saveUser(User user) {
		userRepository.save(user);
	}

	public User getUser(Integer id) {
		return userRepository.findById(id).get();
	}

	public void deleteUser(Integer id) {
		userRepository.deleteById(id);
	}
}