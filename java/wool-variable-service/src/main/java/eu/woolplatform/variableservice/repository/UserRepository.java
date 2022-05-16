package eu.woolplatform.variableservice.repository;

import eu.woolplatform.variableservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

}