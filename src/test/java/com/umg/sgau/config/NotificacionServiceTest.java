package com.umg.sgau.notificacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.umg.sgau.notificacion.entity.*;
import com.umg.sgau.notificacion.repository.*;
import com.umg.sgau.usuario.repository.UsuarioRepository;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.*;

class NotificacionServiceTest {
    private NotificacionRepository notices;
    private DispositivoPushRepository devices;
    private NotificacionService service;

    @SuppressWarnings("unchecked")
    @BeforeEach void setup() {
        notices = mock(NotificacionRepository.class);
        devices = mock(DispositivoPushRepository.class);
        service = new NotificacionService(notices, devices, mock(UsuarioRepository.class), mock(ObjectProvider.class));
    }

    @Test void listAndUnreadAreAlwaysScopedToAuthenticatedUserId() {
        Pageable page = PageRequest.of(0, 20);
        service.list(17L, page);
        service.unread(17L);
        verify(notices).findByUsuarioIdOrderByFechaCreacionDesc(17L, page);
        verify(notices).countByUsuarioIdAndLeidaFalse(17L);
        verify(notices, never()).findAll();
    }

    @Test void eventCreatesRecipientOnlyOnceWithStableEventKey() {
        when(notices.existsByUsuarioIdAndEventKey(17L, "grade:8:v3")).thenReturn(false, true);
        EventoNotificacion event = new EventoNotificacion(17L, "grade:8:v3", "NOTA", "Nota actualizada",
                "Hay una actualización de notas.", "NOTA", 8L, false);

        service.guardarEvento(event);
        service.guardarEvento(event);

        verify(notices, times(1)).save(argThat(n -> n.getUsuarioId().equals(17L)
                && n.getEventKey().equals("grade:8:v3") && n.getDestinoId().equals(8L)));
    }

    @Test void anotherUsersNotificationCannotBeReadOrDeleted() {
        when(notices.findByIdAndUsuarioId(22L, 17L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.read(17L, 22L));
        assertThrows(NoSuchElementException.class, () -> service.delete(17L, 22L));
        verify(notices, never()).delete(any(Notificacion.class));
    }

    @Test void clearAndReadAllUseAuthenticatedUserFilter() {
        service.readAll(17L);
        service.clear(17L);
        verify(notices).readAll(17L);
        verify(notices).deleteByUsuarioId(17L);
    }

    @Test void failedPushIsRetriedAndRecordedWithoutLosingInboxNotice() throws Exception {
        @SuppressWarnings("unchecked") ObjectProvider<FcmSender> provider = mock(ObjectProvider.class);
        FcmSender push = mock(FcmSender.class);
        Notificacion pending = Notificacion.builder().id(41L).usuarioId(17L).eventKey("enrollment:4")
                .tipo("INSCRIPCION").titulo("Inscripción confirmada").mensaje("Hay una actualización.")
                .destinoTipo("CURSO").destinoId(4L).build();
        when(provider.getIfAvailable()).thenReturn(push);
        when(notices.due(any(), any())).thenReturn(List.of(pending));
        when(devices.findByUsuarioId(17L)).thenReturn(List.of(DispositivoPush.builder().token("device-token").build()));
        doThrow(new RuntimeException("FCM temporalmente fuera de servicio")).doNothing()
                .when(push).send(eq("device-token"), anyMap());
        service = new NotificacionService(notices, devices, mock(UsuarioRepository.class), provider);

        service.dispatch();
        assertEquals("ERROR", pending.getEntregaEstado());
        assertEquals(1, pending.getIntentos());
        assertNotNull(pending.getNextAttemptAt());
        service.dispatch();
        assertEquals("ENVIADO", pending.getEntregaEstado());
        assertEquals(2, pending.getIntentos());
        verify(push, times(2)).send(eq("device-token"), anyMap());
    }
}
