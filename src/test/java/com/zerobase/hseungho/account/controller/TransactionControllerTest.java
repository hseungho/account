package com.zerobase.hseungho.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.hseungho.account.dto.CancelBalance;
import com.zerobase.hseungho.account.dto.TransactionDto;
import com.zerobase.hseungho.account.dto.UseBalance;
import com.zerobase.hseungho.account.service.TransactionService;
import com.zerobase.hseungho.account.type.TransactionResultType;
import com.zerobase.hseungho.account.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("컨트롤러 - 거래 사용 성공")
    void successUseBalance() throws Exception {
        // given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(
                        TransactionDto.builder()
                                .accountNumber("1000000000")
                                .transactedAt(LocalDateTime.now())
                                .amount(12345L)
                                .transactionId("transactionId")
                                .transactionResultType(TransactionResultType.S)
                                .build()
                );
        // when
        // then
        mockMvc.perform(post("/transaction/use")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new UseBalance.Request(1L, "2000000000", 3000L)
                    ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.amount").value(12345L));
    }

    @Test
    @DisplayName("컨트롤러 - 거래 취소 성공")
    void successCancelBalance() throws Exception {
        // given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                .willReturn(
                        TransactionDto.builder()
                                .accountNumber("1000000000")
                                .transactedAt(LocalDateTime.now())
                                .amount(54321L)
                                .transactionId("transactionIdForCancel")
                                .transactionResultType(TransactionResultType.S)
                                .build()
                );
        // when
        // then
        mockMvc.perform(post("/transaction/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CancelBalance.Request("transactionId", "2000000000", 3000L)
                        ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
                .andExpect(jsonPath("$.amount").value(54321L));
    }

    @Test
    void successQueryTransaction() throws Exception {
        // given
        given(transactionService.queryTransactionById(anyString()))
                .willReturn(
                        TransactionDto.builder()
                                .accountNumber("1000000000")
                                .transactionType(TransactionType.USE)
                                .transactedAt(LocalDateTime.now())
                                .amount(54321L)
                                .transactionId("transactionIdForCancel")
                                .transactionResultType(TransactionResultType.S)
                                .build()
                );
        // when
        // then
        mockMvc.perform(get("/transaction/12345"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionType").value("USE"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
                .andExpect(jsonPath("$.amount").value(54321L));
    }

}