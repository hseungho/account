package com.zerobase.hseungho.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자가 없습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "계좌가 없습니다."),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 거래가 없습니다."),
    USER_ACCOUNT_UN_MATCH(HttpStatus.FORBIDDEN, "사용자와 계좌의 소유주가 다릅니다."),
    TRANSACTION_ACCOUNT_UN_MATCH(HttpStatus.BAD_REQUEST, "이 거래는 해당 계좌에서 발생한 거래가 아닙니다."),
    ACCOUNT_ALREADY_UNREGISTERED(HttpStatus.BAD_REQUEST, "계좌가 이미 해지되었습니다."),
    BALANCE_NOT_EMPTY(HttpStatus.BAD_REQUEST, "잔액이 있는 계좌는 해지할 수 없습니다."),
    AMOUNT_EXCEED_BALANCE(HttpStatus.BAD_REQUEST, "거래 금액이 계좌 잔액보다 큽니다."),
    MAX_ACCOUNT_PER_USER_10(HttpStatus.BAD_REQUEST, "사용자 최대 계좌는 10개입니다."),
    CANCEL_MUST_FULLY(HttpStatus.BAD_REQUEST, "부분 취소는 허용되지 않습니다."),
    TOO_OLD_ORDER_TO_CANCEL(HttpStatus.BAD_REQUEST, "1년이 지난 거래는 취소가 불가능합니다.")
    ;


    private HttpStatus httpStatus;
    private final String description;
}
