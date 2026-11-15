package com.atlasrh.atlasrh_api.service;

import com.atlasrh.atlasrh_api.dto.LeaveDecisionDTO;
import com.atlasrh.atlasrh_api.dto.LeaveRequestDTO;
import com.atlasrh.atlasrh_api.entity.Employee;
import com.atlasrh.atlasrh_api.entity.LeaveRequest;
import com.atlasrh.atlasrh_api.enums.LeaveStatus;
import com.atlasrh.atlasrh_api.exception.ConflictException;
import com.atlasrh.atlasrh_api.exception.ResourceNotFoundException;
import com.atlasrh.atlasrh_api.mapper.LeaveRequestMapper;
import com.atlasrh.atlasrh_api.repository.EmployeeRepository;
import com.atlasrh.atlasrh_api.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestMapper leaveRequestMapper;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    private Employee manager;
    private Employee subordinate;
    private LeaveRequest leaveRequest;
    private LeaveRequestDTO leaveRequestDTO;

    @BeforeEach
    void setUp() {
        manager = Employee.builder().id(1L).firstName("Jean").lastName("Dupont").build();
        subordinate = Employee.builder().id(2L).firstName("Sophie").lastName("Martin").manager(manager).build();

        leaveRequest = LeaveRequest.builder()
                .id(1L)
                .startDate(LocalDate.of(2026, 12, 1))
                .endDate(LocalDate.of(2026, 12, 5))
                .status(LeaveStatus.EN_ATTENTE)
                .employee(subordinate)
                .build();

        leaveRequestDTO = LeaveRequestDTO.builder()
                .id(1L)
                .status(LeaveStatus.EN_ATTENTE)
                .build();
    }

    // Teste la récupération des demandes en attente d'un manager qui existe -> doit retourner la liste
    @Test
    void shouldReturnPendingRequestsForExistingManager() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(leaveRequestRepository.findByEmployeeManagerIdAndStatus(1L, LeaveStatus.EN_ATTENTE))
                .thenReturn(List.of(leaveRequest));
        when(leaveRequestMapper.toDto(leaveRequest)).thenReturn(leaveRequestDTO);

        List<LeaveRequestDTO> result = leaveRequestService.getPendingForManager(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(LeaveStatus.EN_ATTENTE);
    }

    // Teste la récupération des demandes en attente pour un manager qui n'existe pas -> doit lever ResourceNotFoundException
    @Test
    void shouldThrowNotFoundWhenManagerDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveRequestService.getPendingForManager(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Teste l'approbation d'une demande EN_ATTENTE -> doit réussir et changer le statut en APPROUVEE
    @Test
    void shouldApproveRequestWhenStatusIsPending() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(leaveRequest)).thenReturn(leaveRequest);
        when(leaveRequestMapper.toDto(leaveRequest)).thenReturn(leaveRequestDTO);

        leaveRequestService.approve(1L);

        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveStatus.APPROUVEE);
        verify(leaveRequestRepository, times(1)).save(leaveRequest);
    }

    // Teste l'approbation d'une demande qui n'est PAS en attente (déjà traitée) -> doit lever ConflictException
    @Test
    void shouldThrowConflictWhenApprovingNonPendingRequest() {
        leaveRequest.setStatus(LeaveStatus.APPROUVEE); // déjà traitée

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));

        assertThatThrownBy(() -> leaveRequestService.approve(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("EN_ATTENTE");

        verify(leaveRequestRepository, never()).save(any());
    }

    // Teste le refus d'une demande EN_ATTENTE avec un commentaire fourni -> doit réussir
    @Test
    void shouldRefuseRequestWhenCommentIsProvided() {
        LeaveDecisionDTO decision = LeaveDecisionDTO.builder().comment("Trop de monde en congé déjà").build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(leaveRequest)).thenReturn(leaveRequest);
        when(leaveRequestMapper.toDto(leaveRequest)).thenReturn(leaveRequestDTO);

        leaveRequestService.refuse(1L, decision);

        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveStatus.REFUSEE);
        assertThat(leaveRequest.getComment()).isEqualTo("Trop de monde en congé déjà");
    }

    // Teste le refus d'une demande SANS commentaire -> doit lever ConflictException (règle : un refus exige un commentaire)
    @Test
    void shouldThrowConflictWhenRefusingWithoutComment() {
        LeaveDecisionDTO decision = LeaveDecisionDTO.builder().comment("").build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));

        assertThatThrownBy(() -> leaveRequestService.refuse(1L, decision))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("commentaire");

        verify(leaveRequestRepository, never()).save(any());
    }

    // Teste le refus d'une demande qui n'est pas EN_ATTENTE -> doit lever ConflictException, même avec un commentaire valide
    @Test
    void shouldThrowConflictWhenRefusingNonPendingRequest() {
        leaveRequest.setStatus(LeaveStatus.REFUSEE); // déjà refusée
        LeaveDecisionDTO decision = LeaveDecisionDTO.builder().comment("Un commentaire valide").build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));

        assertThatThrownBy(() -> leaveRequestService.refuse(1L, decision))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("EN_ATTENTE");
    }

    // Teste le calcul des jours ouvrés sur une semaine complète (lundi à vendredi) -> doit compter 5 jours
    @Test
    void shouldCalculateFiveWorkingDaysForFullWeek() {
        int days = LeaveRequestService.calculateWorkingDays(
                LocalDate.of(2026, 11, 16), // lundi
                LocalDate.of(2026, 11, 20)  // vendredi
        );

        assertThat(days).isEqualTo(5);
    }

    // Teste le calcul des jours ouvrés sur une période incluant un week-end -> le week-end ne doit pas être compté
    @Test
    void shouldExcludeWeekendFromWorkingDaysCalculation() {
        int days = LeaveRequestService.calculateWorkingDays(
                LocalDate.of(2026, 11, 16), // lundi
                LocalDate.of(2026, 11, 22)  // dimanche suivant
        );

        assertThat(days).isEqualTo(5); // lundi à vendredi seulement, samedi+dimanche exclus
    }
}