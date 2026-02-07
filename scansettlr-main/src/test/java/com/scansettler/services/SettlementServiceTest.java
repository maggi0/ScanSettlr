package com.scansettler.services;

import com.scansettler.models.Expense;
import com.scansettler.models.Settlement;
import com.scansettler.models.User;
import com.scansettler.repositories.ExpenseRepository;
import com.scansettler.repositories.SettlementRepository;
import com.scansettler.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@MockitoSettings(strictness = Strictness.LENIENT)
public class SettlementServiceTest
{
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private SettlementRepository settlementRepository;
    @Mock
    private UserRepository userRepository;

    private SettlementService settlementService;

    @BeforeEach
    public void setup()
    {
        settlementService = new SettlementService(expenseRepository, settlementRepository, userRepository);
    }


    @ParameterizedTest
    @MethodSource("provideScenarios")
    public void testCalculateSettlements(List<Expense> expenses, Set<Settlement> expectedSettlements)
    {
        Set<String> ids = expenses.stream().map(Expense::getId).collect(Collectors.toSet());

        given(expenseRepository.findAllById(ids)).willReturn(expenses);
        given(userRepository.findById("1")).willReturn(Optional.of(User.builder().username("1").build()));
        given(userRepository.findById("2")).willReturn(Optional.of(User.builder().username("2").build()));
        given(userRepository.findById("3")).willReturn(Optional.of(User.builder().username("3").build()));

        Set<Settlement> actualSettlements = settlementService.calculateSettlements("1", ids);

        assertThat(actualSettlements)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyInAnyOrderElementsOf(expectedSettlements);
    }

    private static Stream<Arguments> provideScenarios()
    {
        return Stream.of(
                oneTransaction(),
                twoBorrowers(),
                circular()
        );
    }

    private static Arguments oneTransaction()
    {
        return Arguments.of(
                List.of(Expense.builder()
                        .id("1")
                        .name("Zakupy")
                        .lenderId("1")
                        .borrowerDetails(Map.of("2", new BigDecimal("30")))
                        .build()
                ),
                Set.of(new Settlement(null, "1", "1", "2", new BigDecimal("30"))));
    }

    private static Arguments twoBorrowers()
    {
        return Arguments.of(
                List.of(Expense.builder()
                        .id("2")
                        .name("Wyjazd")
                        .lenderId("1")
                        .borrowerDetails(Map.of(
                                "2", new BigDecimal("20"),
                                "3", new BigDecimal("10")
                        ))
                        .build()),
                Set.of(
                        new Settlement(null, "1", "1", "2", new BigDecimal("20")),
                        new Settlement(null, "1", "1", "3", new BigDecimal("10"))
                )
        );
    }

    private static Arguments circular()
    {
        return Arguments.of(
                List.of(
                        Expense.builder()
                                .id("3")
                                .name("Obiad")
                                .lenderId("1")
                                .borrowerDetails(Map.of("2", new BigDecimal("50"))).build(),
                        Expense.builder()
                                .id("4")
                                .name("Kolacja")
                                .lenderId("2")
                                .borrowerDetails(Map.of("1", new BigDecimal("20"))).build()
                ),
                Set.of(
                        new Settlement(null, "1", "1", "2", new BigDecimal("30"))
                )
        );
    }


}
