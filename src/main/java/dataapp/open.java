package dataapp;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dataapp.entity.userdata;
import dataapp.entity.userdatablood;
import dataapp.entity.userinformation;

@CrossOrigin
@RestController
@RequestMapping(value = "/open", produces = MediaType.APPLICATION_JSON_VALUE)
public class open {

	EntityManagerFactory emf = Persistence.createEntityManagerFactory("dataapp");
	EntityManager em = emf.createEntityManager();

	@GetMapping("userinfo")
	public ResponseEntity<String> userinfo(@RequestHeader("Token") String token) {

		try {

			String tokenCheck = util.tokencheck(token);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(tokenCheck);

			if (!root.get("active").asBoolean())
				return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

			String userName = root.get("username").asText();
			userinformation user = util.getuser(userName);

			List<userdata> userData = em.createNamedQuery("userdata.finduserid_selected", userdata.class)
					.setParameter(1, user).getResultList();

			if (!Objects.isNull(userData)) {
				String res = mapper.writeValueAsString(userData);
				return new ResponseEntity<String>(res, HttpStatus.OK);

			} else {
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

			}

		} catch (JsonProcessingException e) {
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("chartdata")
	public ResponseEntity<String> chartdata(@RequestHeader("Token") String token) {

		try {

			String tokenCheck = util.tokencheck(token);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(tokenCheck);

			if (!root.get("active").asBoolean())
				return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

			String userName = root.get("username").asText();
			userinformation user = util.getuser(userName);

			List<userdatablood> userData = em.createNamedQuery("userdatablood.finduserid", userdatablood.class)
					.setParameter(1, user).getResultList();

			if (!Objects.isNull(userData)) {
				String res = mapper.writeValueAsString(userData);
				return new ResponseEntity<String>(res, HttpStatus.OK);

			} else {
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

			}

		} catch (JsonProcessingException e) {
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
