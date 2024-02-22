package com.kbtg.bootcamp.posttest.user.service;

import com.kbtg.bootcamp.posttest.exception.ResourceUnavailableException;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicket;
import com.kbtg.bootcamp.posttest.user.model.User;
import com.kbtg.bootcamp.posttest.user.model.UserTicket;
import com.kbtg.bootcamp.posttest.user.model.UserTicketResponse;
import com.kbtg.bootcamp.posttest.user.repository.UserTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
class UserTicketServiceTest {

    @Autowired
    private UserTicketService userTicketService;

    @MockBean
    private UserTicketRepository userTicketRepository;

    @Test
    void testCreateUserTicketTransaction() {
        User user = new User();
        user.setId(1);

        LotteryTicket lotteryTicket = new LotteryTicket();
        lotteryTicket.setId(1);

        UserTicket userTicket = new UserTicket();
        userTicket.setId(1);
        userTicket.setUser(user);
        userTicket.setLottery(lotteryTicket);

        when(userTicketRepository.save(any(UserTicket.class))).thenReturn(userTicket);

        UserTicketResponse actualResult = userTicketService.createUserTicket(user, lotteryTicket);

        verify(userTicketRepository, times(1)).save(any(UserTicket.class));
        assertEquals(userTicket.getId(), actualResult.id());
    }

    @Test
    void testGetUserTicketNumbers() {
        String userId = "1234567890";
        User user1 = new User();
        user1.setId(1);
        user1.setUserId("1234567890");

        User user2 = new User();
        user2.setId(2);
        user2.setUserId("0234567890");

        LotteryTicket lotteryTicket1 = new LotteryTicket();
        lotteryTicket1.setId(1);
        lotteryTicket1.setTicket("123456");

        LotteryTicket lotteryTicket2 = new LotteryTicket();
        lotteryTicket2.setId(2);
        lotteryTicket2.setTicket("000000");

        UserTicket userTicket1 = new UserTicket();
        userTicket1.setUser(user1);
        userTicket1.setLottery(lotteryTicket1);

        UserTicket userTicket2 = new UserTicket();
        userTicket2.setUser(user2);
        userTicket2.setLottery(lotteryTicket2);

        when(userTicketRepository.findByUserUserId(userId)).thenReturn(Arrays.asList(userTicket1, userTicket2));

        List<String> actualResult = userTicketService.getUserLotteryTicketList(userId);

        assertEquals(2, actualResult.size());
        assertEquals("123456", actualResult.get(0));
        assertEquals("000000", actualResult.get(1));
    }

    @Test
    void testGetUserLotteryTicketList() {
        String userId = "123456";
        String ticketId = "1234567890";

        User user = new User();
        user.setId(1);
        user.setUserId(userId);

        LotteryTicket lotteryTicket = new LotteryTicket();
        lotteryTicket.setId(1);
        lotteryTicket.setTicket(ticketId);

        List<UserTicket> userTickets = new ArrayList<>();

        UserTicket userTicket = new UserTicket();
        userTicket.setUser(user);
        userTicket.setLottery(lotteryTicket);

        userTicket.getUser().setUserId(userId);
        userTicket.getLottery().setTicket(ticketId);

        userTickets.add(userTicket);

        when(userTicketRepository.findByUserUserIdAndLotteryTicket(userId, ticketId)).thenReturn(userTickets);

        List<UserTicket> actualResult = userTicketService.getUserLotteryTicketList(userId, ticketId);

        assertEquals(user.getId(), actualResult.get(0).getUser().getId());
        assertEquals(userTickets, actualResult);
    }

    @Test
    void testGetUserLotteryTicketListButThrowsExceptionWhenNoTicketsFound() {
        String userId = "user123";
        String ticketId = "ticket456";

        when(userTicketRepository.findByUserUserIdAndLotteryTicket(userId, ticketId)).thenReturn(new ArrayList<>());

        ResourceUnavailableException exception = assertThrows(ResourceUnavailableException.class, () -> userTicketService.getUserLotteryTicketList(userId, ticketId));

        assertEquals("userId: user123 does not own this ticketId: ticket456", exception.getMessage());
    }
}
