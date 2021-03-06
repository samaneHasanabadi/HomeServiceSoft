package ir.samane.homeservicesoft.services;

import ir.samane.homeservicesoft.model.dao.CustomerDao;
import ir.samane.homeservicesoft.model.entity.ConfirmationToken;
import ir.samane.homeservicesoft.model.entity.Customer;
import ir.samane.homeservicesoft.model.entity.User;
import ir.samane.homeservicesoft.model.enums.RegisterStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService extends UserService {

    private CustomerDao customerDao;
    private ConfirmationTokenService confirmationTokenService;
    private EmailSenderService emailSenderService;

    @Autowired
    public void setCustomerDao(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    @Autowired
    public void setConfirmationTokenService(ConfirmationTokenService confirmationTokenService) {
        this.confirmationTokenService = confirmationTokenService;
    }

    @Autowired
    public void setEmailSenderService(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @Override
    public User registerUser(User user) throws Exception {
        User save = super.registerUser(user);
        Customer customer = (Customer) user;
        customer.setStatus(RegisterStatus.REGISTERED);
        customerDao.save(customer);
        final ConfirmationToken confirmationToken = new ConfirmationToken(customer);
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        sendConfirmationMail(user.getEmail(), confirmationToken.getConfirmationToken());
        return save;
    }

    public void sendConfirmationMail(String userMail, String token) {
        final SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userMail);
        mailMessage.setSubject("Mail Confirmation Link!");
        mailMessage.setFrom("<MAIL>");
        mailMessage.setText(
                "Thank you for registering. Please click on the below link to activate your account.\n" + "http://localhost:8080/confirm/customer?token="
                        + token);
        emailSenderService.sendEmail(mailMessage);
    }

    public void confirmUser(ConfirmationToken confirmationToken) {
        final Customer customer = (Customer) confirmationToken.getUser();
        customer.setStatus(RegisterStatus.APPROVED);
        customerDao.save(customer);
        confirmationTokenService.deleteConfirmationToken(confirmationToken.getId());
    }

    public Customer findById(int id) throws Exception {
        Optional<Customer> customer = customerDao.findById(id);
        if(!customer.isPresent())
            throw new Exception("There is no Customer with this id");
        return customer.get();
    }
}
