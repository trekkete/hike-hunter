package it.trekkete.hikehunter.ui.views.logged;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.trekkete.hikehunter.data.entity.Trip;
import it.trekkete.hikehunter.data.entity.TripChatMessage;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.service.TripChatMessageRepository;
import it.trekkete.hikehunter.data.service.TripRepository;
import it.trekkete.hikehunter.data.service.UserRepository;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import javax.annotation.security.PermitAll;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@PageTitle("Chat escursione")
@Route(value = "trip/:tripId/chat", layout = MainLayout.class)
@PermitAll
public class ChatView extends VerticalLayout implements BeforeEnterObserver {

    private Trip trip;
    private TripChatMessage chat;

    private AuthenticatedUser authenticatedUser;
    private TripRepository tripRepository;
    private TripChatMessageRepository chatRepository;
    private UserRepository userRepository;

    public ChatView(@Autowired AuthenticatedUser authenticatedUser,
                    @Autowired TripRepository tripRepository,
                    @Autowired TripChatMessageRepository chatRepository,
                    @Autowired UserRepository userRepository) {
        this.authenticatedUser = authenticatedUser;
        this.tripRepository = tripRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    public void constructUI() {

        getStyle().set("background-color", "#00680082");
        setMinHeight("100%");

        VerticalLayout container = new VerticalLayout();
        container.addClassNames("esplora-view", "main-container");

        add(container);

        List<TripChatMessage> chat = chatRepository.findByTrip(trip.getId(), Sort.by(Sort.Direction.ASC, "ts"));

        List<MessageListItem> items = new ArrayList<>();
        for (TripChatMessage message : chat) {

            items.add(new MessageListItem(message.getContent(),
                    Instant.ofEpochSecond(message.getTs()),
                    userRepository.findById(message.getUser()).get().getUsername()));
        }

        MessageList list = new MessageList(items);
        list.setSizeFull();

        Scroller scroller = new Scroller(list);
        scroller.setMaxHeight("80%");
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        container.add(scroller);

        MessageInput input = new MessageInput();
        input.setWidthFull();
        input.addSubmitListener(submit -> {

            String message = submit.getValue();
            Long ts = Instant.now().getEpochSecond();

            TripChatMessage newMessage = new TripChatMessage();
            newMessage.setTrip(trip.getId());
            newMessage.setUser(authenticatedUser.get().get().getId());
            newMessage.setTs(ts);
            newMessage.setContent(message);

            chatRepository.save(newMessage);

            chat.add(newMessage);
            items.add(new MessageListItem(message, Instant.ofEpochSecond(ts), authenticatedUser.get().get().getUsername()));

            list.setItems(items);
        });

        container.add(input);

    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

        String id = beforeEnterEvent.getRouteParameters().get("tripId").get();

        this.trip = tripRepository.findById(UUID.fromString(id)).get();

        constructUI();
    }
}
