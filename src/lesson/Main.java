package lesson;

public class Main {

    public static class UntrustworthyMailWorker implements MailService {

        private final RealMailService realMailService = new RealMailService();
        private final MailService[] mailServices;

        public UntrustworthyMailWorker(MailService[] mailServices) {
            this.mailServices = mailServices;
        }

        public RealMailService getRealMailService() {
            return realMailService;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            Sendable inProcess = mail;
            for (MailService ms : mailServices) {
                inProcess = ms.processMail(inProcess);
            }

            return getRealMailService().processMail(inProcess);
        }
    }

    public static class Spy implements MailService {

        private Logger logger = null;

        public Spy(Logger logger) {
            this.logger = logger;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            if (!(mail instanceof MailMessage)) {
                return mail;
            }
            logger.setLevel(Level.ALL);
            if (!mail.getFrom().equals(AUSTIN_POWERS) && !mail.getTo().equals(AUSTIN_POWERS)) {
                logger.log(Level.INFO, "Usual correspondence: from {0} to {1}",
                        new Object[]{mail.getFrom(), mail.getTo()});
            } else {
                logger.log(Level.WARNING, "Detected target mail correspondence: "
                                + "from {0} to {1} \"" + ((MailMessage) mail).getMessage() + "\"",
                        new Object[]{mail.getFrom(), mail.getTo()});
            }
            return mail;
        }
    }

    public static class Thief implements MailService {

        private final int price;
        private int stolenValue;

        public Thief(int price) {
            this.price = price;
        }

        public int getStolenValue() {
            return stolenValue;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            if (mail instanceof MailPackage) {
                if (((MailPackage)mail).getContent().getPrice() >= price) {
                    stolenValue += ((MailPackage)mail).getContent().getPrice();
                    return new MailPackage(mail.getFrom(), mail.getTo(),
                            new Package("stones instead of " + ((MailPackage)mail).getContent().getContent(), 0));
                }
            }
            return mail;
        }
    }

    public static class Inspector implements MailService {

        private static final String[] ILLEGAL_CONTENT =
                new String[]{WEAPONS, BANNED_SUBSTANCE};

        @Override
        public Sendable processMail(Sendable mail) {
            if (mail instanceof MailPackage) {
                MailPackage mailPackage = (MailPackage) mail;
                if (mailPackage.getContent().getContent().contains("stones")) {
                    throw new StolenPackageException();
                }
                for (String illegalString : ILLEGAL_CONTENT) {
                    if (mailPackage.getContent().getContent().contains(illegalString)) {
                        throw new IllegalPackageException();
                    }
                }
            }
            return mail;
        }
    }

    public static class IllegalPackageException extends RuntimeException {

        public IllegalPackageException() {
        }
    }

    public static class StolenPackageException extends RuntimeException {

        public StolenPackageException() {
        }
    }
}
