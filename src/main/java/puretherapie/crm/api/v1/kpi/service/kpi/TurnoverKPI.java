package puretherapie.crm.api.v1.kpi.service.kpi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.data.product.bill.MeansOfPayment;
import puretherapie.crm.data.product.bill.Payment;
import puretherapie.crm.data.product.bill.repository.MeansOfPaymentRepository;
import puretherapie.crm.data.product.bill.repository.PaymentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.api.v1.kpi.service.kpi.TurnoverKPI.TURNOVER_KPI_NAME;

@Slf4j
@AllArgsConstructor
@Service(TURNOVER_KPI_NAME)
public class TurnoverKPI extends ExecutableKPI {

    // Constants.

    public static final String TURNOVER_KPI_NAME = "TurnoverKPI";

    // Variables.

    private MeansOfPaymentRepository meansOfPaymentRepository;
    private PaymentRepository paymentRepository;

    // Methods.

    @Override
    protected KPIRes executeFor(LocalDate begin, LocalDate end) {
        LocalDateTime beginDateTime = LocalDateTime.of(begin, LocalTime.of(0, 0, 0));
        LocalDateTime endDateTime = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0, 0));

        Map<String, Double> mapMeansOfPayment = new HashMap<>();
        List<Payment> payments = new ArrayList<>();

        List<MeansOfPayment> meansOfPayments = meansOfPaymentRepository.findAll();
        for (MeansOfPayment meansOfPayment : meansOfPayments) {
            List<Payment> allPayments =
                    paymentRepository.findByMeansOfPaymentAndCanceledAndPaymentDateGreaterThanEqualAndPaymentDateLessThan(meansOfPayment, false,
                                                                                                                          beginDateTime, endDateTime);
            payments.addAll(allPayments);
            mapMeansOfPayment.put(meansOfPayment.getName(), paymentSum(allPayments));
        }

        List<MeansOfPaymentTurnover> turnoverByMeanOfPayment =
                mapMeansOfPayment.entrySet().stream().map(entry -> new MeansOfPaymentTurnover(entry.getKey(), entry.getValue())).toList();
        return new TurnoverKPIRes(paymentSum(payments), turnoverByMeanOfPayment);
    }

    private double paymentSum(List<Payment> payments) {
        double sum = 0.d;
        for (Payment payment : payments) {
            sum += payment.getAmountPaid();
        }
        return sum;
    }

    // Inner classes.

    public static class TurnoverKPIRes extends KPIRes {

        public TurnoverKPIRes(double totalTurnover, List<MeansOfPaymentTurnover> turnoverByMeanOfPayment) {
            super(TURNOVER_KPI_NAME, new TurnoverKPIPackage(totalTurnover, turnoverByMeanOfPayment));
        }
    }

    public record TurnoverKPIPackage(double totalTurnover, List<MeansOfPaymentTurnover> turnoverByMeanOfPayment) {
    }

    public record MeansOfPaymentTurnover(String meansOfPayment, double turnover) {
    }
}
