package com.kbtg.bootcamp.posttest.user.service;

import com.kbtg.bootcamp.posttest.exception.ResourceUnavailableException;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicket;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicketResponse;
import com.kbtg.bootcamp.posttest.lottery.repository.LotteryTicketRepository;
import com.kbtg.bootcamp.posttest.user.model.User;
import com.kbtg.bootcamp.posttest.user.model.UserTicket;
import com.kbtg.bootcamp.posttest.user.model.UserTicketListResponse;
import com.kbtg.bootcamp.posttest.user.model.UserTicketResponse;
import com.kbtg.bootcamp.posttest.user.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
class UserServiceTest {
    private User user;

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserTicketRepository userTicketRepository;

    @MockBean
    private LotteryTicketRepository lotteryTicketRepository;

    @MockBean
    private UserTicketService userTicketService;

    @BeforeEach
    void testSetup() {
        user = new User();
    }

    @Test
    void testPurchaseLotteryTicketSuccess() {
        user.setId(1);
        user.setUserId("1234567890");
        when(userRepository.findByUserId(user.getUserId())).thenReturn(user);

        LotteryTicket lotteryTicket = new LotteryTicket();
        lotteryTicket.setId(1);
        lotteryTicket.setTicket("123456");
        lotteryTicket.setPrice(80);
        lotteryTicket.setAmount(1);
        when(lotteryTicketRepository.findByTicket(lotteryTicket.getTicket())).thenReturn(lotteryTicket);

        UserTicketResponse userTicketResponse = new UserTicketResponse(1);
        when(userTicketService.createUserTicket(user, lotteryTicket)).thenReturn(userTicketResponse);

        UserTicketResponse actualResult = userService.purchaseLotteryTicket(user.getUserId(), lotteryTicket.getTicket());

        verify(lotteryTicketRepository, times(1)).save(lotteryTicket);
        assertEquals(80, user.getTotalSpent());
        assertEquals(1, user.getTotalLottery());
        assertEquals(userTicketResponse.id(), actualResult.id());
        assertEquals(0, lotteryTicket.getAmount());
    }

    @Test
    void testPurchaseLotteryTicketWithUserNotFound() {
        when(userRepository.findByUserId("9999999999")).thenReturn(null);

        assertThrows(ResourceUnavailableException.class, () -> userService.purchaseLotteryTicket("9999999999", "123456"));
    }

    @Test
    void testPurchaseLotteryTicketWithNotFoundTicket() {
        when(userRepository.findByUserId("1234567890")).thenReturn(user);
        when(lotteryTicketRepository.findByTicket("999999")).thenReturn(null);

        assertThrows(ResourceUnavailableException.class, () -> userService.purchaseLotteryTicket("1234567890", "999999"));

    }

    @Test
    void testPurchaseLotteryTicketWithOutOfStockTicket() {
        LotteryTicket outOfStockTicket = new LotteryTicket();
        outOfStockTicket.setAmount(0);
        when(lotteryTicketRepository.findByTicket("outOfStockTicketId")).thenReturn(outOfStockTicket);

        assertThrows(ResourceUnavailableException.class, () -> userService.purchaseLotteryTicket("1234567890", "888888"));
    }

    @Test
    void testGetUserLotteryTicketList() {
        String userId = "1234567890";
        user.setId(1);
        user.setUserId(userId);
        user.setTotalSpent(260);
        user.setTotalLottery(3);
        List<String> ticketNumbers = Arrays.asList("123456", "123456", "666666");

        when(userRepository.findByUserId(userId)).thenReturn(user);
        when(userTicketService.getUserLotteryTicketList(userId)).thenReturn(ticketNumbers);

        UserTicketListResponse actualResult = userService.getUserLotteryTicketList(userId);

        verify(userRepository, times(1)).findByUserId(userId);
        verify(userTicketService, times(1)).getUserLotteryTicketList(userId);
        assertEquals(user.getTotalLottery(), actualResult.count());
        assertEquals(user.getTotalSpent(), actualResult.cost());
        assertEquals(ticketNumbers, actualResult.tickets());
    }

    @Test
    void getUserLotteryTicketListButUserIdNotFound() {
        when(userRepository.findByUserId("9999999999")).thenReturn(null);

        assertThrows(ResourceUnavailableException.class, () -> userService.getUserLotteryTicketList("9999999999"));
    }

    @Test
     void testSellLotteryTicketsSuccess() {
        String userId = "1234567890";
        String ticketId = "123456";

        User user = new User();
        user.setUserId(userId);
        user.setTotalLottery(4);
        user.setTotalSpent(8);

        LotteryTicket lotteryTicket = new LotteryTicket();
        lotteryTicket.setTicket(ticketId);
        lotteryTicket.setAmount(5);
        lotteryTicket.setPrice(2);

        List<UserTicket> userTickets = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserTicket userTicket = new UserTicket();
            userTicket.setUser(user);
            userTicket.setLottery(lotteryTicket);
            userTickets.add(userTicket);
        }

        when(userRepository.findByUserId(userId)).thenReturn(user);
        when(userTicketService.getUserLotteryTicketList(userId, ticketId)).thenReturn(userTickets);

        LotteryTicketResponse actualResult = userService.sellLotteryTickets(userId, ticketId);

        verify(lotteryTicketRepository, times(1)).save(lotteryTicket);
        verify(userTicketRepository, times(1)).deleteAll(userTickets);
        assertEquals(2, user.getTotalSpent());
        assertEquals(1, user.getTotalLottery());
        assertEquals(8, lotteryTicket.getAmount());
        assertEquals(ticketId, actualResult.ticket());
    }
}
