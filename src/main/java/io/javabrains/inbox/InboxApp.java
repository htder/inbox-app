package io.javabrains.inbox;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.javabrains.inbox.email.Email;
import io.javabrains.inbox.email.EmailRepository;
import io.javabrains.inbox.emaillist.EmailListItem;
import io.javabrains.inbox.emaillist.EmailListItemKey;
import io.javabrains.inbox.emaillist.EmailListItemRepository;
import io.javabrains.inbox.folders.Folder;
import io.javabrains.inbox.folders.FolderRepository;
import io.javabrains.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@RestController
public class InboxApp {

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	EmailListItemRepository emailListItemRepository;

	@Autowired
	EmailRepository emailRepository;

	@Autowired
	UnreadEmailStatsRepository unreadEmailStatsRepository;

	public static void main(String[] args) {
		SpringApplication.run(InboxApp.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void init() {
		folderRepository.save(new Folder("htder", "Inbox", "blue"));
		folderRepository.save(new Folder("htder", "Sent", "green"));
		folderRepository.save(new Folder("htder", "Important", "yellow"));

		unreadEmailStatsRepository.decrementUnreadCount("htder", "Inbox");
		unreadEmailStatsRepository.decrementUnreadCount("htder", "Inbox");
		unreadEmailStatsRepository.decrementUnreadCount("htder", "Inbox");

		for (int i = 0; i < 10; i++) {
			EmailListItemKey key = new EmailListItemKey();
			key.setId("htder");
			key.setLabel("Inbox");
			key.setTimeUUID(Uuids.timeBased());

			EmailListItem item = new EmailListItem();
			item.setKey(key);
			item.setTo(List.of("htder", "abc", "def"));
			item.setSubject("Subject " + i);
			item.setUnread(true);

			emailListItemRepository.save(item);

			Email email = new Email();
			email.setId(key.getTimeUUID());
			email.setFrom("htder");
			email.setSubject(item.getSubject());
			email.setBody("Body " + i);
			email.setTo(item.getTo());

			emailRepository.save(email);
		}
	}

}
