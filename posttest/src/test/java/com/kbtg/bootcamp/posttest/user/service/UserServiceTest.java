package com.kbtg.bootcamp.posttest.user.service;

import com.kbtg.bootcamp.posttest.exception.ResourceUnavailableException;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicket;
import com.kbtg.bootcamp.posttest.lottery.repository.LotteryTicketRepository;
import com.kbtg.bootcamp.posttest.user.model.User;
import com.kbtg.bootcamp.posttest.user.model.UserTicket;
import com.kbtg.bootcamp.posttest.user.model.UserTicketListResponse;
import com.kbtg.bootcamp.posttest.user.model.UserTicketResponse;
import com.kbtg.bootcamp.posttest.user.repository.UserRepository;
import com.kbtg.bootcamp.posttest.user.repository.UserTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
class UserServiceTest {

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

    @Test
    void testPurchaseLotteryTicketSuccess() {
        User user = new User();
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
        when(userTicketService.createUserTicketTransaction(user, lotteryTicket)).thenReturn(userTicketResponse);

        UserTicketResponse actualResult = userService.purchaseLotteryTicket(user.getUserId(), lotteryTicket.getTicket());

        assertEquals(userTicketResponse.id(), actualResult.id());
    }

    @Test
    void testPurchaseLotteryTicketWithUserNotFound() {
        when(userRepository.findByUserId("9999999999")).thenReturn(null);

        assertThrows(ResourceUnavailableException.class, () -> userService.purchaseLotteryTicket("9999999999", "123456"));
    }

    @Test
    void testPurchaseLotteryTicketWithNotFoundTicket() {
        when(userRepository.findByUserId("1234567890")).thenReturn(new User());
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
        List<String> expectedTicketNumbers = Arrays.asList("123456", "123456", "666666");
        User user = new User();
        user.setId(1);
        user.setUserId("1234567890");
        user.setTotalSpent(260);
        user.setTotalLottery(3);

        LotteryTicket lotteryTicket1 = new LotteryTicket();
        lotteryTicket1.setTicket("123456");
        lotteryTicket1.setPrice(80);
        lotteryTicket1.setAmount(2);

        LotteryTicket lotteryTicket2 = new LotteryTicket();
        lotteryTicket2.setTicket("666666");
        lotteryTicket2.setPrice(100);
        lotteryTicket2.setAmount(1);

        UserTicket userTicket1 = new UserTicket();
        userTicket1.setLottery(lotteryTicket1);

        UserTicket userTicket2 = new UserTicket();
        userTicket2.setLottery(lotteryTicket2);

        when(userRepository.findByUserId("1234567890")).thenReturn(user);
        List<UserTicket> userTickets = Arrays.asList(userTicket1, userTicket1, userTicket2);
        when(userTicketRepository.findByUserUserId("1234567890")).thenReturn(userTickets);
        UserTicketListResponse actualResult = userService.getUserLotteryTicketList("1234567890");

        verify(userRepository, times(1)).findByUserId(user.getUserId());
        verify(userTicketRepository, times(1)).findByUserUserId(user.getUserId());
        assertEquals(user.getTotalLottery(), actualResult.count());
        assertEquals(user.getTotalSpent(), actualResult.cost());
        assertEquals(expectedTicketNumbers, actualResult.tickets());
    }

    @Test
    void getUserLotteryTicketListButUserIdNotFound() {
        when(userRepository.findByUserId("9999999999")).thenReturn(null);

        assertThrows(ResourceUnavailableException.class, () -> userService.getUserLotteryTicketList("9999999999"));
    }
}
