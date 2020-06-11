package photoalbum.app.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import photoalbum.app.data.CommentStorage;
import photoalbum.app.data.MarkStorage;
import photoalbum.app.data.PhotoStorage;
import photoalbum.app.data.ProfileStorage;
import photoalbum.app.data.RelationshipsStorage;
import photoalbum.app.data.TagStorage;
import photoalbum.app.domain.comment.CommentService;
import photoalbum.app.domain.dto.CommentJsonDTO;
import photoalbum.app.domain.dto.MarkJsonDTO;
import photoalbum.app.domain.dto.PhotoJsonDTO;
import photoalbum.app.domain.dto.ProfileJsonDTO;
import photoalbum.app.domain.dto.TagJsonDTO;
import photoalbum.app.domain.mark.MarkService;
import photoalbum.app.domain.model.Photo;
import photoalbum.app.domain.model.Relationships;
import photoalbum.app.domain.model.Status;
import photoalbum.app.domain.photo.PhotoService;
import photoalbum.app.domain.profile.ProfileService;
import photoalbum.app.domain.tag.TagService;
import photoalbum.app.spring.ProfileDetailsImpl;

@RestController
@RequestMapping("/ajax")
public class AjaxController {
	@Autowired
	RelationshipsStorage relationshipsStorage;
	
	@Autowired
	ProfileStorage profileStorage;
	
	@Autowired
	ProfileService profileService;
	
	@Autowired
	PhotoService photoService;
	
	@Autowired
	PhotoStorage photoStorage;
	
	@Autowired
	TagStorage tagStorage;
	
	@Autowired
	TagService tagService;
	
	@Autowired
	CommentStorage commentStorage;
	
	@Autowired
	CommentService commentService;
	
	@Autowired
	MarkService markService;
	
	@Autowired
	MarkStorage markStorage;
	
	@RequestMapping(value = "/add-friend")
	public void addFriend(@RequestParam("n") String nickname) {
		ProfileDetailsImpl profileDetails = (ProfileDetailsImpl)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		Long profileId = profileStorage.getIdByNickname(nickname);
		Long loginProfileId = profileStorage.getIdByNickname(profileDetails.getNickname());
		if(loginProfileId != profileId) {
			Relationships relationship;
			try {
				relationship = relationshipsStorage.findRelationshipsByUsers(loginProfileId, profileId);
				Status status = relationship.getStatus();
				if(status == Status.SUBSCRIBER)
					relationshipsStorage.unsubscribe(relationship.getId());
				if (status == Status.FRIEND)
					relationshipsStorage.deleteFriend(profileId, loginProfileId, relationship.getId());
			} catch(EmptyResultDataAccessException e) {
				try {
					relationship = relationshipsStorage.findRelationshipsByUsers(profileId, loginProfileId);
					Status status = relationship.getStatus();
					if(status == Status.SUBSCRIBER)
						relationshipsStorage.acceptInvite(relationship.getId());
					if (status == Status.FRIEND)
						relationshipsStorage.deleteFriend(profileId, loginProfileId, relationship.getId());
				} catch(EmptyResultDataAccessException e1) {
					relationshipsStorage.sendInvite(loginProfileId, profileId);
				}
			}
		}
	}
	
	@RequestMapping(value = "/my-profile")
	public boolean showAddFriendButton(@RequestParam("n") String nick) {
		ProfileDetailsImpl profileDetails = (ProfileDetailsImpl)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(profileDetails.getNickname().equals(nick))	
			return true;
		else
			return false;
	}
	
	@RequestMapping(value = "/friend-list/{divId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ProfileJsonDTO> friendList(@RequestParam("n") String nick, @PathVariable String divId) {
		return profileService.usersByUserAsJson(profileStorage.getIdByNickname(HtmlUtils.htmlEscape(nick)), HtmlUtils.htmlEscape(divId));
	}
	
	@RequestMapping(value = "/photos", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PhotoJsonDTO> photoList(@RequestParam("n") String nick) {
		//return photoService.photosByUserAsJson(profileStorage.getIdByNickname(HtmlUtils.htmlEscape(nick)));
		return photoService.photosByUserAsJson(photoStorage.getPhotosByUser(profileStorage.getIdByNickname(HtmlUtils.htmlEscape(nick))));
	}
	
	@RequestMapping(value = "/tags", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TagJsonDTO> tagList(@RequestParam("id") Long photoId) {
		//return tagService.tagsByUserAsJson(tagStorage.getTagsByPhoto(photoId));
		return tagService.tagsByPhotoAsJson(tagStorage.getTagsByPhoto(photoId));
	}
	
	@RequestMapping(value = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<CommentJsonDTO> commentList(@RequestParam("id") Long photoId) {
		return commentService.commentsByPhotoAsJson(commentStorage.getCommentsByPhoto(photoId));
	}
	
	@RequestMapping(value = "/marks", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<MarkJsonDTO> markList(@RequestParam("id") Long photoId) {
		return markService.marksByPhotoAsJson(markStorage.getMarksByPhoto(photoId));
	}
}
