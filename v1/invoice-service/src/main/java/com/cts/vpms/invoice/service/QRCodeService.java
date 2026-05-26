package com.cts.vpms.invoice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QRCodeService {

    private static final int QR_WIDTH  = 250;
    private static final int QR_HEIGHT = 250;

    @Value("${billing.upi.vpa:parking@upi}")
    private String merchantVpa;

    @Value("${billing.upi.name:City Parking}")
    private String merchantName;

    public String generatePaymentQR(long invoiceId, BigDecimal amount)
            throws WriterException, IOException {

        String upiPayload = String.format(
                "upi://pay?pa=%s&pn=%s&am=%s&cu=INR&tn=INV-%d",
                merchantVpa,
                merchantName.replace(" ", "%20"),
                amount.toPlainString(),
                invoiceId
        );

        log.info("Generating QR | invoiceId={} amount={} vpa={}", invoiceId, amount, merchantVpa);

        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(
                upiPayload, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        return Base64.getEncoder().encodeToString(out.toByteArray());
    }
}