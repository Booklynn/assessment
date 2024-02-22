package com.kbtg.bootcamp.posttest.user.service;

import com.kbtg.bootcamp.posttest.exception.ResourceUnavailableException;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicket;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicketResponse;
import com.kbtg.bootcamp.posttest.lottery.repository.LotteryTicketRepository;
import com.kbtg.bootcamp.posttest.lottery.service.LotteryService;
import com.kbtg.bootcamp.posttest.user.model.User;
import com.kbtg.bootcamp.posttest.user.model.UserTicket;
import com.kbtg.bootcamp.posttest.user.model.UserTicketListResponse;
import com.kbtg.bootcamp.posttest.user.model.UserTicketResponse;
import com.kbtg.bootcamp.posttest.user.repository.UserRepository;
import com.kbtg.bootcamp.posttest.user.repository.UserTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserTicketRepository userTicketRepository;
    private final LotteryTicketRepository lotteryTicketRepository;
    private final UserTicketService userTicketService;
    private final LotteryService lotteryService;

    @Autowired
    public UserService(
            UserRepository userRepository,
            LotteryTicketRepository lotteryTicketRepository,
            UserTicketService userTicketService,
            UserTicketRepository userTicketRepository,
            LotteryService lotteryService
    ) {
        this.userRepository = userRepository;
        this.lotteryTicketRepository = lotteryTicketRepository;
        this.userTicketService = userTicketService;
        this.userTicketRepository = userTicketRepository;
        this.lotteryService = lotteryService;
    }

    @Transactional
    public UserTicketResponse purchaseLotteryTicket(String userId, String ticketId) {
        User user = getUser(userId);
        LotteryTicket lotteryTicket = lotteryService.getLotteryTicket(ticketId);

        UserTicketResponse userTicket = userTicketService.createUserTicket(user, lotteryTicket);

        this.updateUserPurchaseLotteryTicketActivity(user, lotteryTicket.getPrice());
        
        lotteryTicket.setAmount(lotteryTicket.getAmount() - 1);
        lotteryTicketRepository.save(lotteryTicket);

        return new UserTicketResponse(userTicket.id());
    }

    private User getUser(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new ResourceUnavailableException("userId: " + userId + " not found");
        }
        return user;
    }

    @Transactional
    protected void updateUserPurchaseLotteryTicketActivity(User user, int price) {
        user.setTotalSpent(user.getTotalSpent() + price);
        user.setTotalLottery(user.getTotalLottery() + 1);
        userRepository.save(user);
    }

    @Transactional
    protected void updateUserSellLotteryTicketActivity(User user, int userTicketLength, int lotteryTicketPrice) {
        int totalSpent = user.getTotalSpent();
        int totalLottery = user.getTotalLottery();

        totalSpent -= userTicketLength * lotteryTicketPrice;
        totalLottery -= userTicketLength;

        user.setTotalSpent(totalSpent);
        user.setTotalLottery(totalLottery);
        userRepository.save(user);
    }

    public UserTicketListResponse getUserLotteryTicketList(String userId) {
        User user = getUser(userId);

        int totalSpent = user.getTotalSpent();
        int totalLottery = user.getTotalLottery();

        List<String> userTickets = userTicketService.getUserLotteryTicketList(userId);

        return new UserTicketListResponse(userTickets, totalLottery, totalSpent);
    }

    @Transactional
    public LotteryTicketResponse sellLotteryTickets(String userId, String ticketId) {
        User user = userRepository.findByUserId(userId);
        List<UserTicket> userTickets = userTicketService.getUserLotteryTicketList(userId, ticketId);
        LotteryTicket lotteryTicket = userTickets.get(0).getLottery();

        int updatedAmount = lotteryTicket.getAmount() + userTickets.size();

        lotteryTicket.setAmount(updatedAmount);
        lotteryTicketRepository.save(lotteryTicket);

        this.updateUserSellLotteryTicketActivity(user, userTickets.size(), lotteryTicket.getPrice());
        userTicketRepository.deleteAll(userTickets);

        return new LotteryTicketResponse(lotteryTicket.getTicket());
    }

}

