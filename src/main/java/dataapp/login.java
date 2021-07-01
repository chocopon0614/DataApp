package dataapp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dataapp.entity.userdatablood;
import dataapp.entity.userinformation;
import dataapp.util.hashutil;
import dataapp.util.jwtutil;

@RestController
@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
public class login {

	@PostMapping("userlogin")
	public ResponseEntity<String> userLogin(@RequestParam("username") final String UserName,
			@RequestParam("password") final String PassWord) {

		String hashedPassword = null;

		try {
			hashedPassword = hashutil.getsha256(PassWord);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("dataapp");
		EntityManager em = emf.createEntityManager();

		userinformation user = em.createNamedQuery("userinformation.findbyusername", userinformation.class)
				.setParameter(1, UserName).getSingleResult();

		if (!(user == null)) {
			String dbPassword = user.getPassword();
			if (hashedPassword.equals(dbPassword)) {

				String jwttoken = jwtutil.createjwt(UserName, dbPassword);
				String res = "{\"JWT\" : \"" + jwttoken + "\" }";

				return new ResponseEntity<String>(res, HttpStatus.OK);

			} else {
				return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);

			}

		} else {
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("register")
	public ResponseEntity<String> register(@RequestParam("username") final String username,
			@RequestParam("password") final String password) throws Exception {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("dataapp");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = null;

		EntityManagerFactory emf2 = Persistence.createEntityManagerFactory("dataapp");
		EntityManager em2 = emf2.createEntityManager();
		EntityTransaction tx2 = null;

		try {
			String hash_password = hashutil.getsha256(password);

			tx = em.getTransaction();
			tx.begin();

			userinformation userinfo = new userinformation();
			userinfo.setUsername(username);
			userinfo.setPassword(hash_password);
			userinfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
			userinfo.setModifiedTime(new Timestamp(System.currentTimeMillis()));

			em.persist(userinfo);

			tx.commit();

			tx2 = em2.getTransaction();
			tx2.begin();

			userinformation user = em2.createNamedQuery("userinformation.findbyusername", userinformation.class)
					.setParameter(1, username).getSingleResult();

			userdatablood userblood = new userdatablood();
			userblood.setUserinformation(user);
			userblood.setFpg(0);
			userblood.setGtp(0);
			userblood.setHdl(0);
			userblood.setLdl(0);
			userblood.setTg(0);

			em2.persist(userblood);

			tx2.commit();

			return new ResponseEntity<String>(HttpStatus.OK);

		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();

			if (tx2 != null && tx2.isActive())
				tx2.rollback();

			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			em.close();
		}

	}

	@DeleteMapping("userdelete")
	public ResponseEntity<String> userDelete(@RequestParam("jwt") final String jwt) throws Exception {

		String UserName = jwtutil.varifyjwt(jwt, "username");

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("DataApp");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = null;

		try {

			tx = em.getTransaction();
			tx.begin();

			em.createNamedQuery("userinformation.deletebyusername", userinformation.class).setParameter(1, UserName)
					.executeUpdate();

			tx.commit();

			return ResponseEntity.ok().build();

		} catch (RuntimeException e) {
			if (tx != null && tx.isActive())
				tx.rollback();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

		} finally {
			em.close();
		}

	}

	@PostMapping("authorization")
	public ResponseEntity<String> authorization(@RequestParam("jwt") final String jwt) {

		try {

			String username = jwtutil.varifyjwt(jwt, "username");
			String password = jwtutil.varifyjwt(jwt, "password");

			String res = "{\"username\" : \"" + username + "\" , \"password\" : \"" + password + "\"}";
			return new ResponseEntity<String>(res, HttpStatus.OK);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("authurl")
	public ResponseEntity<String> authurl(@RequestHeader("authorization") final String auth) {

		Charset charset = StandardCharsets.UTF_8;
		String tmp = auth.split(" ")[1];

		byte[] b = Base64.getDecoder().decode(tmp.getBytes(charset));
		String de1 = new String(b, charset);

		String username = de1.split(":")[0];
		String hash_password = de1.split(":")[1];

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("DataApp");
		EntityManager em = emf.createEntityManager();

		userinformation UserObj = em.createNamedQuery("userinformation.findbyusername", userinformation.class)
				.setParameter(1, username).getSingleResult();

		if (!(UserObj == null)) {
			String DbPass = UserObj.getPassword();
			if (hash_password.equals(DbPass)) {

				return ResponseEntity.ok().build();

			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}

		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

}
