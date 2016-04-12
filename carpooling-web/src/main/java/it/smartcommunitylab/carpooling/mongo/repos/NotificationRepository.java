/**
 * Copyright 2015 Smart Community Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	@Query(value="{'targetUserId':?0, 'status': false}", count = true)
	Long countReadByTargetUserId(String targetUserId);
	
	@Query("{'targetUserId':?0, 'status': false}")
	List<Notification> findUnReadNotifications(String targetUserId);
	
	@Query("{'targetUserId':?0, 'status': true}")
	List<Notification> findReadNotifications(String targetUserId);
	
	@Query("{'targetUserId':?0}")
	List<Notification> findAllNotifications(String targetUserId);
	
	@Query("{'id':?0, 'targetUserId':?1}")
	Notification findByIdAndTargetUserId(String id, String targetUserId);

	@Query("{'travelId':?0, 'type':?1}")
	List<Notification> findByTravelIdAndNotificationType(String travelId, String type);
	
	@Query("{'travelId':?0}")
	List<Notification> findByTravelId(String travelId);

}
