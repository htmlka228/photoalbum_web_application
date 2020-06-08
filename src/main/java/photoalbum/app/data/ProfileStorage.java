package photoalbum.app.data;

import java.util.List;

import photoalbum.app.domain.model.Profile;

public interface ProfileStorage {
	
	List<Profile> findAll();
	
	int countByEmail(String email);
	
	void save(Profile u);
	
	Profile findByEmailAndEnabledTrue(String email);
	
	void addAvatar(Profile profile);
	
	void addBackground(Profile profile);
	
	Profile getProfileByNickname(String nickname);
	
	Long getIdByNickname(String nickname);

	Profile findByActivationCode(String code);
	
}
