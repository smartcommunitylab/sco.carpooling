package it.smartcommunitylab.carpooling.mongo.repos;

import java.util.List;

import it.smartcommunitylab.carpooling.model.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface NotificationRepository extends PagingAndSortingRepository<Notification, String> {

	@Query("{'targetUserId':?0}")
	Page<Notification> findByTargetUserId(String targetUserId, Pageable pageable);
	
	@Query("{'targetUserId':?0, 'status': false}")
	List<Notification> findUnReadNotifications(String targetUserId);
	
	@Query("{'targetUserId':?0, 'status': true}")
	List<Notification> findReadNotifications(String targetUserId);
	
	@Query("{'targetUserId':?0}")
	List<Notification> findAllNotifications(String targetUserId);

}
