package rut.miit.tech.summer_hackathon.repository;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ModeratorRepository moderatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    private final Faker faker = new Faker(new Locale("ru"));

    private final List<String> DEPARTMENTS = List.of(
            "Научно-образовательный центр \"Техническая эксплуатация железнодорожного транспорта и безопасность движения\" Института управления и цифровых технологий",
            "Учебный отдел Института управления и цифровых технологий",
            "Отдел информатизации Института управления и цифровых технологий",
            "Кафедра «Вычислительные системы, сети и информационная безопасность»",
            "Кафедра «Железнодорожные станции и транспортные узлы»",
            "Кафедра «Логистические транспортные системы и технологии»",
            "Кафедра «Управление эксплуатационной работой и безопасностью на транспорте»",
            "Кафедра «Логистика и управление транспортными системами»",
            "Кафедра «Управление транспортным бизнесом и интеллектуальные системы»",
            "Кафедра «Химия и инженерная экология»",
            "Лаборатория «Процессы и аппараты защиты окружающей среды и промышленной экологии» кафедры «Химия и инженерная экология»",
            "Лаборатория «Пробоотбор и пробоподготовка» кафедры «Химия и инженерная экология»",
            "Лаборатория «Экологические информационные системы» кафедры «Химия и инженерная экология»",
            "Лаборатория «Экологический мониторинг и средства контроля окружающей среды (физические методы контроля)» кафедры «Химия и инженерная экология»",
            "Лаборатория «Экологический мониторинг и средства контроля окружающей среды (химические методы контроля)» кафедры «Химия и инженерная экология»",
            "Кафедра «Цифровые технологии управления транспортными процессами»",
            "Кафедра «Коммерческая эксплуатация транспорта и тарифы»",
            "Кафедра «Бизнес-аналитика перевозочного процесса и бережливые технологии»",
            "Центр инновационных образовательных программ \"Высшая школа управления\"",
            "Центр мультимодальных транспортных систем Института управления и цифровых технологий",
            "Отдел дистанционных образовательных технологий Института управления и цифровых технологий",
            "Научно-образовательный центр \"Независимые комплексные транспортные исследования\"",
            "Научно-образовательный центр \"Центр компетенций системы управления качеством\" Института управления и цифровых технологий",
            "Научно-образовательный центр \"Центр развития цифровых технологий и формирования единого информационного пространства транспортной отрасли\"",
            "Научно-образовательный центр \"Центр отраслевой экспертно-аналитической деятельности\"",
            "Научно-образовательный центр \"Центр стратегических инновационных исследований и разработок\"",
            "Научно-образовательный внедренческий центр \"Инновационные технологии управления производственно-экономическими процессами\"",
            "Центр развития инфраструктуры, технологий, бизнеса вокзальных и транспортно-пересадочных комплексов Института управления и цифровых технологий",
            "Научно-образовательный центр прогрессивных технологий перевозочного процесса, интеллектуальных систем организации движения и комплексной безопасности на транспорте",
            "Научно-образовательный центр \"Цифровая информационно-аналитическая оптика\""
    );

    private final List<String> ACADEMIC_POSITIONS = List.of(
            "Профессор", "Доцент", "Старший преподаватель", "Преподаватель", "Ассистент",
            "Заведующий кафедрой", "Декан", "Проректор", "Научный сотрудник",
            "Старший научный сотрудник", "Ведущий научный сотрудник", "Главный научный сотрудник",
            "Инженер", "Старший инженер", "Ведущий инженер", "Главный инженер",
            "Лаборант", "Старший лаборант", "Техник", "Программист", "Системный администратор",
            "Методист", "Старший методист", "Специалист", "Ведущий специалист", "Главный специалист"
    );

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Начинаем создание тестовых данных...");

        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        moderatorRepository.deleteAll();

        System.out.println("Создаем модераторов...");
        List<Moderator> moderators = createModerators();
        System.out.println("Создано модераторов: " + moderators.size());

        System.out.println("Создаем департаменты...");
        List<Department> departments = createDepartments(moderators);
        System.out.println("Создано департаментов: " + departments.size());

        System.out.println("Создаем пользователей...");
        createUsers(departments, moderators);
        System.out.println("Тестовые данные созданы успешно!");
    }

    private List<Moderator> createModerators() {
        List<Moderator> moderators = new ArrayList<>();

        int moderatorCount = faker.number().numberBetween(50, 71);
        System.out.println("Планируем создать " + moderatorCount + " модераторов");

        for (int i = 0; i < moderatorCount; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String middleName = faker.name().nameWithMiddle().split(" ")[1]; // Получаем отчество
            String login = transliterate(firstName.toLowerCase() + "." + lastName.toLowerCase());

            Moderator moderator = Moderator.builder()
                    .login(login)
                    .password(passwordEncoder.encode("123"))
                    .firstName(firstName)
                    .lastName(lastName)
                    .middleName(middleName)
                    .build();

            moderator = moderatorRepository.save(moderator);
            moderators.add(moderator);

            if (i < 5) { // Логируем первые 5 модераторов для проверки
                System.out.println("Создан модератор: ID=" + moderator.getId() +
                        ", Login=" + moderator.getLogin() +
                        ", Name=" + moderator.getFirstName() + " " + moderator.getLastName());
            }
        }

        return moderators;
    }

    private List<Department> createDepartments(List<Moderator> moderators) {
        List<Department> departments = new ArrayList<>();

        // Проверяем, что список модераторов не пустой
        if (moderators.isEmpty()) {
            throw new RuntimeException("Список модераторов пуст. Невозможно создать департаменты.");
        }

        for (int i = 0; i < DEPARTMENTS.size(); i++) {
            String departmentName = DEPARTMENTS.get(i);
            Moderator assignedModerator = moderators.get(faker.random().nextInt(moderators.size()));

            List<String> tags = generateDepartmentTags(departmentName);

            Department department = Department.builder()
                    .name(departmentName)
                    .moderator(assignedModerator)
                    .tags(tags)
                    .build();

            department = departmentRepository.save(department);
            departments.add(department);

            if (i < 5) { // Логируем первые 5 департаментов для проверки
                System.out.println("Создан департамент: ID=" + department.getId() +
                        ", Name=" + department.getName() +
                        ", Moderator ID=" + department.getModerator().getId() +
                        ", Moderator Login=" + department.getModerator().getLogin());
            }
        }

        return departments;
    }

    private void createUsers(List<Department> departments, List<Moderator> moderators) {
        int userCount = faker.number().numberBetween(300, 501);

        List<Moderator> validModerators = moderators.stream()
                .filter(m -> m != null && m.getId() != null)
                .toList();

        if (validModerators.isEmpty()) {
            throw new RuntimeException("Нет валидных модераторов для назначения пользователям.");
        }

        List<Moderator> moderatorsCycle = new ArrayList<>(validModerators);
        int moderatorIndex = 0;

        for (int i = 0; i < userCount; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String middleName = faker.name().nameWithMiddle().split(" ")[1];

            String email = generateEmail(firstName, lastName);
            String businessPhone = generateRussianPhone();
            String personalPhone = generateRussianPhone();
            String position = ACADEMIC_POSITIONS.get(faker.random().nextInt(ACADEMIC_POSITIONS.size()));
            Long officeNumber = (long) faker.number().numberBetween(100, 999);
            String note = generateUserNote();

            Moderator assignedModerator = moderatorsCycle.get(moderatorIndex);
            moderatorIndex = (moderatorIndex + 1) % moderatorsCycle.size();

            User user = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .middleName(middleName)
                    .email(email)
                    .businessPhone(businessPhone)
                    .personalPhone(personalPhone)
                    .position(position)
                    .officeNumber(officeNumber)
                    .note(note)
                    .moderator(assignedModerator) // Теперь всегда не null
                    .departments(new ArrayList<>())
                    .build();

            try {
                user = userRepository.save(user);
                assignUserToDepartments(user, departments);

                if (i % 50 == 0) {
                    System.out.println("Создан пользователь " + i + ": " +
                            user.getLastName() + " " + user.getFirstName() +
                            " -> Модератор: " + assignedModerator.getLogin());
                }
            } catch (Exception e) {
                System.err.println("Ошибка при создании пользователя: " + e.getMessage());
            }
        }

        System.out.println("Всего создано пользователей: " + userCount);
        System.out.println("Назначено модераторов: " + validModerators.size());
    }

    private void assignUserToDepartments(User user, List<Department> departments) {
        if (faker.number().numberBetween(1, 101) <= 20) {
            return;
        }

        int departmentCount = faker.number().numberBetween(1, 4);
        Set<Department> selectedDepartments = new HashSet<>();

        while (selectedDepartments.size() < departmentCount) {
            Department randomDepartment = departments.get(faker.random().nextInt(departments.size()));
            selectedDepartments.add(randomDepartment);
        }

        user.getDepartments().addAll(selectedDepartments);
        userRepository.save(user);
    }

    private List<String> generateDepartmentTags(String departmentName) {
        List<String> tags = new ArrayList<>();
        String lowerName = departmentName.toLowerCase();

        // Добавляем ключевые слова из названия департамента как теги
        if (lowerName.contains("кафедра")) tags.add("кафедра");
        if (lowerName.contains("лаборатория")) tags.add("лаборатория");
        if (lowerName.contains("центр")) tags.add("центр");
        if (lowerName.contains("отдел")) tags.add("отдел");
        if (lowerName.contains("транспорт")) tags.add("транспорт");
        if (lowerName.contains("цифров")) tags.add("цифровые технологии");
        if (lowerName.contains("экология")) tags.add("экология");
        if (lowerName.contains("безопасность")) tags.add("безопасность");
        if (lowerName.contains("управление")) tags.add("управление");
        if (lowerName.contains("логистик")) tags.add("логистика");
        if (lowerName.contains("информ")) tags.add("информатизация");

        List<String> randomTags = List.of("образование", "наука", "исследования", "технологии",
                "инновации", "разработка", "аналитика", "мониторинг");
        for (int i = 0; i < faker.number().numberBetween(1, 4); i++) {
            tags.add(randomTags.get(faker.random().nextInt(randomTags.size())));
        }

        return tags;
    }

    private String generateEmail(String firstName, String lastName) {
        String transliteratedFirst = transliterate(firstName.toLowerCase());
        String transliteratedLast = transliterate(lastName.toLowerCase());
        return transliteratedFirst + "." + transliteratedLast + "@rut-miit.ru";
    }

    private String generateRussianPhone() {
        String[] codes = {"495", "499", "812", "343", "383", "391", "846", "863", "831", "473"};
        String areaCode = codes[faker.random().nextInt(codes.length)];

        return "+7-" + areaCode + "-" +
                faker.number().numberBetween(100, 1000) + "-" +
                faker.number().numberBetween(10, 100) + "-" +
                faker.number().numberBetween(10, 100);
    }

    private String generateUserNote() {
        List<String> noteTemplates = List.of(
                "Кандидат технических наук",
                "Доктор технических наук",
                "Кандидат экономических наук",
                "Имеет опыт работы в отрасли " + faker.number().numberBetween(5, 25) + " лет",
                "Специалист по железнодорожному транспорту",
                "Ведет курсы по направлению " + faker.educator().course(),
                "Участник международных конференций",
                "Автор научных публикаций",
                "Руководитель научных проектов",
                "Эксперт в области транспортных технологий"
        );

        return noteTemplates.get(faker.random().nextInt(noteTemplates.size()));
    }

    private String transliterate(String text) {
        String[][] map = {
                {"а", "a"}, {"б", "b"}, {"в", "v"}, {"г", "g"}, {"д", "d"}, {"е", "e"}, {"ё", "yo"},
                {"ж", "zh"}, {"з", "z"}, {"и", "i"}, {"й", "y"}, {"к", "k"}, {"л", "l"}, {"м", "m"},
                {"н", "n"}, {"о", "o"}, {"п", "p"}, {"р", "r"}, {"с", "s"}, {"т", "t"}, {"у", "u"},
                {"ф", "f"}, {"х", "kh"}, {"ц", "ts"}, {"ч", "ch"}, {"ш", "sh"}, {"щ", "sch"},
                {"ъ", ""}, {"ы", "y"}, {"ь", ""}, {"э", "e"}, {"ю", "yu"}, {"я", "ya"}
        };

        String result = text;
        for (String[] pair : map) {
            result = result.replace(pair[0], pair[1]);
        }
        return result;
    }
}