package com.kbtg.bootcamp.posttest.lottery.service;

import com.kbtg.bootcamp.posttest.exception.DuplicationException;
import com.kbtg.bootcamp.posttest.exception.ResourceUnavailableException;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicket;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicketListResponse;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicketRequest;
import com.kbtg.bootcamp.posttest.lottery.model.LotteryTicketResponse;
import com.kbtg.bootcamp.posttest.lottery.repository.LotteryTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LotteryService {

    private final LotteryTicketRepository lotteryTicketRepository;

    @Autowired
    public LotteryService(LotteryTicketRepository lotteryTicketRepository) {
        this.lotteryTicketRepository = lotteryTicketRepository;
    }

    @Transactional
    public LotteryTicketResponse createLotteryTicket(LotteryTicketRequest lotteryTicketRequest) {
        checkLotteryDuplication(lotteryTicketRequest);

        LotteryTicket ticket = new LotteryTicket();
        ticket.setTicket(lotteryTicketRequest.getTicket());
        ticket.setPrice(lotteryTicketRequest.getPrice());
        ticket.setAmount(lotteryTicketRequest.getAmount());

        LotteryTicket savedTicket = lotteryTicketRepository.save(ticket);

        return new LotteryTicketResponse(savedTicket.getTicket());
    }

    private void checkLotteryDuplication(LotteryTicketRequest lotteryTicketRequest) {
        LotteryTicket existingTicket = lotteryTicketRepository.findByTicket(lotteryTicketRequest.getTicket());
        if (existingTicket != null) {
            throw new DuplicationException("ticketId: " + lotteryTicketRequest.getTicket() + " already existing");
        }
    }

    public LotteryTicketListResponse getLotteryTicketList() {
        List<String> ticketNumbers = lotteryTicketRepository
                .findAll()
                .stream()
                .map(LotteryTicket::getTicket)
                .collect(Collectors.toList());

        return new LotteryTicketListResponse(ticketNumbers);
    }

    public LotteryTicket getLotteryTicket(String ticketId) {
        LotteryTicket lotteryTicket = lotteryTicketRepository.findByTicket(ticketId);
        if (lotteryTicket == null || lotteryTicket.getAmount() <= 0) {
            throw new ResourceUnavailableException("ticketId: " + ticketId + " unavailable (not found or out of stock)");
        }
        return lotteryTicket;
    }
}
