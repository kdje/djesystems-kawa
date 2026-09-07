package com.djesystems.kawa.customer.application;

import com.djesystems.kawa.customer.domain.NotificationStatus;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerNotificationDeviceEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.CustomerNotificationDeviceRepository;
import com.djesystems.kawa.customer.infrastructure.persistence.NotificationOutboxEntity;
import com.djesystems.kawa.customer.infrastructure.persistence.NotificationOutboxRepository;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationOutboxDispatcher {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationOutboxDispatcher.class);

    private final NotificationOutboxRepository outboxRepository;
    private final CustomerNotificationDeviceRepository deviceRepository;
    private final FirebaseMessaging firebaseMessaging;

    public NotificationOutboxDispatcher(
            NotificationOutboxRepository outboxRepository,
            CustomerNotificationDeviceRepository deviceRepository,
            FirebaseMessaging firebaseMessaging) {

        this.outboxRepository = outboxRepository;
        this.deviceRepository = deviceRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    @Scheduled(
        fixedDelayString =
            "${kawa.notification.dispatch-delay-ms:2000}"
    )
    @Transactional
    @SuppressWarnings("deprecation")
    public void dispatchPendingNotifications() {

        List<NotificationOutboxEntity> notifications =
                outboxRepository
                    .findByStatusOrderByCreatedAtAsc(
                        NotificationStatus.PENDING
                    );

        for (NotificationOutboxEntity notification : notifications) {

            List<CustomerNotificationDeviceEntity> devices =
                    deviceRepository
                        .findByPublicKawaIdAndActiveTrue(
                            notification.getPublicKawaId()
                        );

            /*
             * Pas encore de token enregistré :
             * on laisse la notification PENDING.
             */
            if (devices.isEmpty()) {

                log.debug(
                    "No active FCM device for publicKawaId={}",
                    notification.getPublicKawaId()
                );

                continue;
            }

            boolean sentAtLeastOnce = false;

            for (CustomerNotificationDeviceEntity device : devices) {

                Message message =
                        Message.builder()
                            .setToken(device.getFcmToken())
                            .setNotification(
                                Notification.builder()
                                    .setTitle(notification.getTitle())
                                    .setBody(notification.getBody())
                                    .build()
                            )
                            .putData(
                                "type",
                                notification.getNotificationType()
                            )
                            .putData(
                                "eventId",
                                notification.getEventId()
                            )
                            .putData(
                                "publicKawaId",
                                notification.getPublicKawaId()
                            )
                            .putData(
                                "payload",
                                notification.getPayload() != null
                                    ? notification.getPayload()
                                    : "{}"
                            )
                            .build();

                try {

                    String messageId =
                            firebaseMessaging.send(message);

                    sentAtLeastOnce = true;

                    log.info(
                        "FCM notification sent: eventId={}, publicKawaId={}, messageId={}",
                        notification.getEventId(),
                        notification.getPublicKawaId(),
                        messageId
                    );

                } catch (FirebaseMessagingException ex) {

                    log.warn(
                        "Unable to send FCM notification: eventId={}, publicKawaId={}, error={}",
                        notification.getEventId(),
                        notification.getPublicKawaId(),
                        ex.getMessage()
                    );

                    if (ex.getMessagingErrorCode()
                            == MessagingErrorCode.UNREGISTERED) {

                        device.deactivate();
                    }
                }
            }

            if (sentAtLeastOnce) {
                notification.markSent();
            } else {
                notification.markFailed();
            }
        }
    }
}