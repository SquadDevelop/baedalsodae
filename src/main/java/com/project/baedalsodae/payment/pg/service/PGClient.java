package com.project.baedalsodae.payment.pg.service;

import com.project.baedalsodae.payment.pg.dto.PGCancelRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentResponse;

public interface PGClient {
    PGPaymentResponse pay(PGPaymentRequest request);

    PGPaymentResponse cancel(PGCancelRequest request);
}
