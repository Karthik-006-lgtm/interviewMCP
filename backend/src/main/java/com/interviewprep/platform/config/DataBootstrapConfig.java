package com.interviewprep.platform.config;

import com.interviewprep.platform.entity.Company;
import com.interviewprep.platform.entity.RoleProfile;
import com.interviewprep.platform.repository.CompanyRepository;
import com.interviewprep.platform.repository.RoleProfileRepository;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class DataBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(DataBootstrapConfig.class);

    @Bean
    @Order(2) // Run after admin bootstrap
    public ApplicationRunner bootstrapRolesAndCompanies(
            RoleProfileRepository roleRepository,
            CompanyRepository companyRepository
    ) {
        return args -> {
            // Check if data already exists
            if (roleRepository.count() > 0 && companyRepository.count() > 0) {
                log.info("Roles and companies already exist, skipping bootstrap");
                return;
            }

            log.info("Bootstrapping roles and companies...");

            // Create all 31 roles
            createRoles(roleRepository);

            // Create 18 companies
            createCompanies(companyRepository);

            log.info("Bootstrap complete: {} roles, {} companies", 
                roleRepository.count(), companyRepository.count());
        };
    }

    private void createRoles(RoleProfileRepository repository) {
        List<RoleData> roles = Arrays.asList(
            new RoleData("Java Developer", 
                "Build and scale backend systems with Java, Spring, and relational data stores.",
                "[\"Java\",\"Spring\",\"Spring Boot\",\"SQL\",\"Microservices\",\"REST APIs\"]",
                "[\"coding\",\"system design\",\"debugging\",\"API design\",\"performance\",\"ownership\"]"),
            new RoleData("Python Developer",
                "Develop automation, APIs, analytics workflows, and ML-adjacent services in Python.",
                "[\"Python\",\"FastAPI\",\"Flask\",\"Pandas\",\"SQL\",\"Automation\"]",
                "[\"problem solving\",\"backend design\",\"data handling\",\"testing\",\"scripting\",\"ownership\"]"),
            new RoleData("Full Stack Developer",
                "Ship end-to-end product features across frontend, backend, API, and database layers.",
                "[\"React\",\"TypeScript\",\"Java\",\"Spring\",\"PostgreSQL\",\"REST APIs\"]",
                "[\"feature design\",\"cross-functional delivery\",\"trade-offs\",\"testing\",\"scalability\",\"UX awareness\"]"),
            new RoleData("Data Analyst",
                "Turn messy data into useful decisions through analysis, dashboards, and stakeholder communication.",
                "[\"Python\",\"SQL\",\"Analytics\",\"Statistics\",\"Visualization\",\"Dashboards\"]",
                "[\"data storytelling\",\"metric design\",\"business context\",\"SQL depth\",\"insight communication\",\"prioritization\"]"),
            new RoleData("DevOps Engineer",
                "Improve deployment speed, reliability, and observability across infrastructure and delivery pipelines.",
                "[\"Docker\",\"Kubernetes\",\"AWS\",\"Terraform\",\"CI/CD\",\"Linux\"]",
                "[\"incident response\",\"automation\",\"observability\",\"resilience\",\"cost-awareness\",\"platform thinking\"]"),
            new RoleData("Frontend Engineer",
                "Create performant, usable, and maintainable interfaces across modern web applications.",
                "[\"React\",\"TypeScript\",\"JavaScript\",\"HTML\",\"CSS\",\"Tailwind CSS\"]",
                "[\"UI architecture\",\"performance\",\"accessibility\",\"state management\",\"testing\",\"product thinking\"]"),
            new RoleData("Backend Engineer",
                "Design stable backend services, data models, and integration-heavy application layers.",
                "[\"Java\",\"Spring\",\"PostgreSQL\",\"REST APIs\",\"Microservices\",\"SQL\"]",
                "[\"API design\",\"data modeling\",\"performance\",\"resilience\",\"debugging\",\"trade-offs\"]"),
            new RoleData("Data Scientist",
                "Build predictive models, run experiments, and deliver machine learning solutions that drive business decisions.",
                "[\"Python\",\"Machine Learning\",\"Statistics\",\"TensorFlow\",\"PyTorch\",\"Data Analysis\"]",
                "[\"model building\",\"experimentation\",\"statistical thinking\",\"ML ops\",\"communication\",\"business impact\"]"),
            new RoleData("Machine Learning Engineer",
                "Deploy scalable ML systems and optimize model performance in production environments.",
                "[\"Python\",\"TensorFlow\",\"PyTorch\",\"MLOps\",\"Kubernetes\",\"Cloud Platforms\"]",
                "[\"model deployment\",\"scalability\",\"performance optimization\",\"monitoring\",\"system design\",\"experimentation\"]"),
            new RoleData("Cloud Architect",
                "Design and implement cloud infrastructure solutions with focus on scalability, security, and cost optimization.",
                "[\"AWS\",\"Azure\",\"GCP\",\"Terraform\",\"Cloud Security\",\"Architecture Design\"]",
                "[\"system design\",\"security\",\"cost optimization\",\"scalability\",\"migration strategies\",\"governance\"]"),
            new RoleData("Site Reliability Engineer",
                "Ensure system reliability, performance, and availability through automation and proactive monitoring.",
                "[\"Kubernetes\",\"Docker\",\"Prometheus\",\"Grafana\",\"Python\",\"Go\"]",
                "[\"incident management\",\"automation\",\"monitoring\",\"capacity planning\",\"post-mortems\",\"on-call\"]"),
            new RoleData("Security Engineer",
                "Protect systems and data through security architecture, threat modeling, and vulnerability management.",
                "[\"Security\",\"Penetration Testing\",\"OWASP\",\"Cryptography\",\"Network Security\",\"Compliance\"]",
                "[\"threat modeling\",\"vulnerability assessment\",\"incident response\",\"secure coding\",\"compliance\",\"risk management\"]"),
            new RoleData("Product Manager",
                "Define product strategy, prioritize features, and drive cross-functional teams to deliver customer value.",
                "[\"Product Strategy\",\"Roadmapping\",\"User Research\",\"Data Analysis\",\"Stakeholder Management\",\"Agile\"]",
                "[\"strategic thinking\",\"prioritization\",\"communication\",\"user empathy\",\"decision making\",\"metrics\"]"),
            new RoleData("UX Designer",
                "Create intuitive user experiences through research, wireframing, prototyping, and usability testing.",
                "[\"Figma\",\"User Research\",\"Wireframing\",\"Prototyping\",\"Usability Testing\",\"Design Systems\"]",
                "[\"user empathy\",\"visual design\",\"interaction design\",\"research methods\",\"collaboration\",\"iteration\"]"),
            new RoleData("UI Developer",
                "Implement pixel-perfect interfaces with focus on responsiveness, accessibility, and performance.",
                "[\"HTML\",\"CSS\",\"JavaScript\",\"React\",\"Vue\",\"Responsive Design\"]",
                "[\"CSS mastery\",\"responsive design\",\"accessibility\",\"performance\",\"cross-browser\",\"attention to detail\"]"),
            new RoleData("Mobile Developer",
                "Build native or cross-platform mobile applications for iOS and Android platforms.",
                "[\"React Native\",\"Flutter\",\"Swift\",\"Kotlin\",\"Mobile UI\",\"API Integration\"]",
                "[\"mobile development\",\"platform guidelines\",\"performance\",\"offline support\",\"app lifecycle\",\"testing\"]"),
            new RoleData("QA Engineer",
                "Ensure software quality through test planning, automation, and comprehensive quality assurance practices.",
                "[\"Automation Testing\",\"Selenium\",\"Jest\",\"Cypress\",\"Test Planning\",\"Bug Tracking\"]",
                "[\"test strategy\",\"automation\",\"attention to detail\",\"communication\",\"debugging\",\"quality mindset\"]"),
            new RoleData("Database Administrator",
                "Manage database systems ensuring performance, security, backup, and recovery procedures.",
                "[\"PostgreSQL\",\"MySQL\",\"MongoDB\",\"Database Tuning\",\"Backup\",\"Security\"]",
                "[\"performance tuning\",\"disaster recovery\",\"security\",\"query optimization\",\"capacity planning\",\"monitoring\"]"),
            new RoleData("Business Analyst",
                "Bridge business needs and technical solutions through requirements gathering and process analysis.",
                "[\"Requirements Analysis\",\"SQL\",\"Business Process\",\"Documentation\",\"Stakeholder Management\",\"JIRA\"]",
                "[\"analytical thinking\",\"communication\",\"documentation\",\"process mapping\",\"stakeholder management\",\"problem solving\"]"),
            new RoleData("Scrum Master",
                "Facilitate agile teams, remove blockers, and ensure smooth sprint execution and continuous improvement.",
                "[\"Agile\",\"Scrum\",\"JIRA\",\"Facilitation\",\"Coaching\",\"Team Building\"]",
                "[\"facilitation\",\"conflict resolution\",\"coaching\",\"metrics\",\"ceremonies\",\"servant leadership\"]"),
            new RoleData("Technical Writer",
                "Create clear documentation, API guides, and user manuals for technical products and services.",
                "[\"Technical Writing\",\"Documentation\",\"API Documentation\",\"Markdown\",\"Git\",\"Content Management\"]",
                "[\"clarity\",\"audience awareness\",\"technical understanding\",\"structure\",\"collaboration\",\"tool proficiency\"]"),
            new RoleData("Solutions Architect",
                "Design end-to-end technical solutions aligning business requirements with technology capabilities.",
                "[\"System Design\",\"Cloud Architecture\",\"Integration Patterns\",\"Microservices\",\"API Design\",\"Enterprise Architecture\"]",
                "[\"holistic thinking\",\"trade-off analysis\",\"communication\",\"technology breadth\",\"business alignment\",\"scalability\"]"),
            new RoleData("Blockchain Developer",
                "Build decentralized applications and smart contracts on blockchain platforms.",
                "[\"Solidity\",\"Ethereum\",\"Web3\",\"Smart Contracts\",\"Cryptography\",\"Blockchain Architecture\"]",
                "[\"blockchain fundamentals\",\"smart contract security\",\"gas optimization\",\"DApp development\",\"consensus\",\"testing\"]"),
            new RoleData("Game Developer",
                "Create engaging gaming experiences through game mechanics, graphics, and performance optimization.",
                "[\"Unity\",\"Unreal Engine\",\"C#\",\"C++\",\"Game Physics\",\"3D Graphics\"]",
                "[\"game mechanics\",\"performance optimization\",\"graphics programming\",\"player experience\",\"debugging\",\"creativity\"]"),
            new RoleData("AI Engineer",
                "Develop artificial intelligence solutions including NLP, computer vision, and generative AI applications.",
                "[\"Deep Learning\",\"NLP\",\"Computer Vision\",\"Transformers\",\"OpenAI\",\"LangChain\"]",
                "[\"model selection\",\"fine-tuning\",\"prompt engineering\",\"evaluation\",\"deployment\",\"ethical AI\"]"),
            new RoleData("Data Engineer",
                "Build and maintain data pipelines, warehouses, and infrastructure for analytics and ML workloads.",
                "[\"Apache Spark\",\"Airflow\",\"SQL\",\"Python\",\"Data Warehousing\",\"ETL\"]",
                "[\"pipeline design\",\"data quality\",\"performance\",\"scalability\",\"orchestration\",\"data modeling\"]"),
            new RoleData("Cybersecurity Analyst",
                "Monitor, detect, and respond to security threats while maintaining security posture.",
                "[\"SIEM\",\"Threat Detection\",\"Incident Response\",\"Network Security\",\"Firewalls\",\"Vulnerability Management\"]",
                "[\"threat analysis\",\"incident handling\",\"security tools\",\"compliance\",\"forensics\",\"risk assessment\"]"),
            new RoleData("iOS Developer",
                "Develop native iOS applications using Swift and Apple development frameworks.",
                "[\"Swift\",\"SwiftUI\",\"UIKit\",\"Xcode\",\"iOS SDK\",\"App Store\"]",
                "[\"iOS development\",\"Apple guidelines\",\"performance\",\"memory management\",\"testing\",\"App Store submission\"]"),
            new RoleData("Android Developer",
                "Create native Android applications using Kotlin and Android development tools.",
                "[\"Kotlin\",\"Android SDK\",\"Jetpack Compose\",\"Android Studio\",\"Material Design\",\"Play Store\"]",
                "[\"Android development\",\"Material Design\",\"performance\",\"compatibility\",\"testing\",\"Play Store guidelines\"]"),
            new RoleData("Engineering Manager",
                "Lead engineering teams, manage technical projects, and drive team growth and performance.",
                "[\"Team Leadership\",\"Project Management\",\"Technical Strategy\",\"Hiring\",\"Performance Management\",\"Agile\"]",
                "[\"people management\",\"strategic thinking\",\"delegation\",\"conflict resolution\",\"career development\",\"technical depth\"]"),
            new RoleData("Platform Engineer",
                "Build internal developer platforms and tools that improve engineering productivity and workflow.",
                "[\"Kubernetes\",\"Docker\",\"CI/CD\",\"Infrastructure as Code\",\"Developer Tools\",\"Automation\"]",
                "[\"platform thinking\",\"developer experience\",\"automation\",\"scalability\",\"documentation\",\"feedback loops\"]")
        );

        for (RoleData data : roles) {
            RoleProfile role = new RoleProfile();
            role.setName(data.name);
            role.setSummary(data.summary);
            role.setCoreSkills(data.coreSkills);
            role.setInterviewFocusAreas(data.interviewFocusAreas);
            repository.save(role);
        }
        
        log.info("Created {} roles", roles.size());
    }

    private void createCompanies(CompanyRepository repository) {
        List<CompanyData> companies = Arrays.asList(
            new CompanyData("Quantum AI Research", "https://quantumai.tech", "hr@quantumai.tech", "Sarah Chen",
                "Dr. Michael Zhang", 450,
                "Founded in 2018, Quantum AI Research pioneers next-generation AI systems combining quantum computing with deep learning.",
                "Innovation-first culture with emphasis on research publications, open collaboration, and cutting-edge technology exploration.",
                "[\"research depth\",\"algorithmic thinking\",\"publication quality\",\"collaboration\",\"innovation\"]",
                "AI Engineer", "Machine Learning Engineer", "Data Scientist", "Python Developer", "Data Engineer"),
            new CompanyData("SecureNet Technologies", "https://securenet.io", "talent@securenet.io", "James Martinez",
                "Lisa Thompson", 890,
                "Enterprise cybersecurity leader since 2012, protecting Fortune 500 companies with advanced threat detection and response platforms.",
                "Security-first mindset with strong emphasis on compliance, continuous learning, and proactive threat intelligence.",
                "[\"security awareness\",\"compliance knowledge\",\"incident response\",\"threat modeling\",\"ethical hacking\"]",
                "Security Engineer", "Cybersecurity Analyst", "DevOps Engineer", "Backend Engineer", "Cloud Architect"),
            new CompanyData("DataFlow Engineering", "https://dataflow.systems", "careers@dataflow.systems", "Priya Patel",
                "Robert Kim", 320,
                "Real-time data infrastructure company building the next generation of streaming analytics and data pipeline solutions.",
                "Data-driven culture valuing reliability, scalability, and elegant system design with strong engineering practices.",
                "[\"system design\",\"data architecture\",\"scalability\",\"performance\",\"pipeline reliability\"]",
                "Data Engineer", "Backend Engineer", "Site Reliability Engineer", "DevOps Engineer", "Python Developer"),
            new CompanyData("MobileFirst Studios", "https://mobilefirst.app", "jobs@mobilefirst.app", "Amanda Rodriguez",
                "David Park", 180,
                "Award-winning mobile app development studio creating consumer apps with millions of downloads across iOS and Android platforms.",
                "Product-focused culture emphasizing user experience, rapid iteration, and cross-platform excellence.",
                "[\"mobile UX\",\"performance\",\"platform guidelines\",\"user feedback\",\"app store optimization\"]",
                "iOS Developer", "Android Developer", "Mobile Developer", "UX Designer", "QA Engineer"),
            new CompanyData("CloudScale Infrastructure", "https://cloudscale.cloud", "hiring@cloudscale.cloud", "Thomas Lee",
                "Jennifer Wu", 620,
                "Cloud infrastructure platform enabling enterprises to build, deploy, and scale applications across multi-cloud environments.",
                "Engineering excellence culture with focus on automation, infrastructure as code, and developer experience.",
                "[\"cloud architecture\",\"automation\",\"scalability\",\"cost optimization\",\"reliability\"]",
                "Cloud Architect", "DevOps Engineer", "Site Reliability Engineer", "Platform Engineer", "Backend Engineer"),
            new CompanyData("GameForge Interactive", "https://gameforge.games", "careers@gameforge.games", "Emily Chen",
                "Marcus Johnson", 550,
                "Independent game studio known for immersive multiplayer experiences and cutting-edge graphics technology since 2015.",
                "Creative and collaborative environment valuing gameplay innovation, technical excellence, and player community.",
                "[\"game mechanics\",\"performance\",\"player experience\",\"graphics programming\",\"creativity\"]",
                "Game Developer", "Backend Engineer", "DevOps Engineer", "QA Engineer", "UX Designer"),
            new CompanyData("FinTech Solutions Group", "https://fintechsolutions.finance", "talent@fintechsolutions.finance", "Raj Kumar",
                "Michelle Anderson", 780,
                "Financial technology platform revolutionizing digital payments, lending, and wealth management for modern consumers.",
                "High-performance culture emphasizing security, compliance, data accuracy, and customer trust.",
                "[\"financial domain\",\"security\",\"compliance\",\"accuracy\",\"scalability\"]",
                "Java Developer", "Backend Engineer", "Full Stack Developer", "Security Engineer", "Data Analyst"),
            new CompanyData("HealthTech Innovations", "https://healthtech-inn.com", "hr@healthtech-inn.com", "Dr. Laura Martinez",
                "Kevin Brown", 410,
                "Healthcare technology company building patient-centric digital health solutions and telemedicine platforms.",
                "Mission-driven culture focused on healthcare impact, data privacy, regulatory compliance, and patient safety.",
                "[\"healthcare domain\",\"HIPAA compliance\",\"data privacy\",\"reliability\",\"user empathy\"]",
                "Full Stack Developer", "Backend Engineer", "Security Engineer", "UX Designer", "QA Engineer"),
            new CompanyData("EcoTech Systems", "https://ecotech.green", "jobs@ecotech.green", "Grace Wilson",
                "Carlos Rodriguez", 290,
                "Sustainability-focused tech company building IoT solutions for energy management and environmental monitoring.",
                "Impact-driven culture combining environmental mission with technical innovation and hardware-software integration.",
                "[\"IoT systems\",\"embedded programming\",\"data analysis\",\"sustainability awareness\",\"system integration\"]",
                "Backend Engineer", "Python Developer", "Data Engineer", "DevOps Engineer", "Frontend Engineer"),
            new CompanyData("StreamMedia Networks", "https://streammedia.tv", "careers@streammedia.tv", "Daniel Kim",
                "Sophia Taylor", 950,
                "Leading video streaming platform delivering high-quality content to millions of viewers globally with adaptive streaming technology.",
                "Scale-focused engineering culture emphasizing performance, reliability, global CDN optimization, and user experience.",
                "[\"video streaming\",\"scalability\",\"performance\",\"CDN\",\"reliability\"]",
                "Backend Engineer", "Site Reliability Engineer", "Full Stack Developer", "DevOps Engineer", "Data Engineer"),
            new CompanyData("Blockchain Ventures Inc", "https://blockchainventures.crypto", "talent@blockchainventures.crypto", "Alex Morgan",
                "Natalie Green", 220,
                "Decentralized finance platform building next-generation blockchain solutions for transparent and secure digital transactions.",
                "Crypto-native culture valuing decentralization, transparency, security audits, and community governance.",
                "[\"blockchain architecture\",\"smart contract security\",\"cryptography\",\"DeFi protocols\",\"consensus mechanisms\"]",
                "Blockchain Developer", "Backend Engineer", "Security Engineer", "Full Stack Developer", "DevOps Engineer"),
            new CompanyData("AutoDrive Technologies", "https://autodrive.tech", "hr@autodrive.tech", "Victor Chen",
                "Rachel Adams", 680,
                "Autonomous vehicle technology company developing self-driving systems with advanced sensor fusion and AI-powered decision making.",
                "Safety-critical engineering culture emphasizing rigorous testing, simulation, real-world validation, and ethical AI.",
                "[\"autonomous systems\",\"machine learning\",\"safety\",\"real-time processing\",\"sensor fusion\"]",
                "Machine Learning Engineer", "AI Engineer", "Backend Engineer", "Data Engineer", "Site Reliability Engineer"),
            new CompanyData("SaaS Platform Co", "https://saasplatform.io", "jobs@saasplatform.io", "Monica Singh",
                "Tyler Johnson", 540,
                "B2B SaaS platform providing enterprise workflow automation and business intelligence tools to mid-market companies.",
                "Customer-centric culture focused on product reliability, integration capabilities, and continuous feature delivery.",
                "[\"SaaS architecture\",\"API design\",\"integration patterns\",\"multi-tenancy\",\"customer success\"]",
                "Full Stack Developer", "Backend Engineer", "Frontend Engineer", "DevOps Engineer", "Product Manager"),
            new CompanyData("EdTech Learning Systems", "https://edtech-learning.edu", "careers@edtech-learning.edu", "Dr. Patricia Lee",
                "Andrew Miller", 370,
                "Educational technology platform transforming online learning with adaptive curricula and data-driven student engagement.",
                "Education-focused culture combining pedagogical expertise with technology innovation and accessibility commitment.",
                "[\"education domain\",\"user experience\",\"accessibility\",\"data analytics\",\"engagement metrics\"]",
                "Full Stack Developer", "Frontend Engineer", "Data Scientist", "UX Designer", "Backend Engineer"),
            new CompanyData("LogiChain Solutions", "https://logichain.supply", "talent@logichain.supply", "Frank Zhang",
                "Catherine Brown", 490,
                "Supply chain management platform optimizing logistics, inventory, and delivery operations for global enterprises.",
                "Operations-focused culture emphasizing system reliability, real-time visibility, optimization algorithms, and data accuracy.",
                "[\"supply chain domain\",\"optimization\",\"real-time systems\",\"data integration\",\"analytics\"]",
                "Backend Engineer", "Data Engineer", "Full Stack Developer", "Data Analyst", "DevOps Engineer"),
            new CompanyData("RoboTech Dynamics", "https://robotech.ai", "hr@robotech.ai", "Dr. Alan Cooper",
                "Olivia Martinez", 340,
                "Robotics company building intelligent automation solutions for manufacturing and warehouse operations with computer vision.",
                "Hardware-software integration culture valuing embedded systems, real-time control, computer vision, and mechatronics.",
                "[\"robotics\",\"computer vision\",\"embedded systems\",\"real-time processing\",\"control systems\"]",
                "AI Engineer", "Machine Learning Engineer", "Backend Engineer", "Python Developer", "DevOps Engineer"),
            new CompanyData("SocialConnect Platform", "https://socialconnect.social", "jobs@socialconnect.social", "Jessica Wang",
                "Brian Thompson", 820,
                "Social networking platform connecting professionals worldwide with AI-powered recommendations and content discovery.",
                "Community-focused culture emphasizing user safety, content moderation, scalable architecture, and engagement metrics.",
                "[\"social systems\",\"recommendation algorithms\",\"content moderation\",\"scalability\",\"user safety\"]",
                "Full Stack Developer", "Backend Engineer", "Machine Learning Engineer", "Frontend Engineer", "Data Scientist"),
            new CompanyData("InsurTech Innovations", "https://insurtech-inn.insure", "careers@insurtech-inn.insure", "Mark Anderson",
                "Linda Davis", 430,
                "Insurance technology platform modernizing policy management, claims processing, and risk assessment with machine learning.",
                "Regulated industry culture balancing innovation with compliance, data security, accuracy, and customer trust.",
                "[\"insurance domain\",\"compliance\",\"data accuracy\",\"risk assessment\",\"automation\"]",
                "Java Developer", "Backend Engineer", "Data Scientist", "Full Stack Developer", "Security Engineer")
        );

        for (CompanyData data : companies) {
            Company company = new Company();
            company.setName(data.name);
            company.setWebsite(data.website);
            company.setHrContact(data.hrContact);
            company.setHiringManager(data.hiringManager);
            company.setOwnerName(data.ownerName);
            company.setEmployeeCount(data.employeeCount);
            company.setCompanyHistory(data.companyHistory);
            company.setCulture(data.culture);
            company.setInterviewFocusAreas(data.interviewFocusAreas);
            company.setSupportedRoles(new LinkedHashSet<>(data.supportedRoles));
            repository.save(company);
        }
        
        log.info("Created {} companies", companies.size());
    }

    private static class RoleData {
        String name, summary, coreSkills, interviewFocusAreas;
        RoleData(String name, String summary, String coreSkills, String interviewFocusAreas) {
            this.name = name;
            this.summary = summary;
            this.coreSkills = coreSkills;
            this.interviewFocusAreas = interviewFocusAreas;
        }
    }

    private static class CompanyData {
        String name, website, hrContact, hiringManager, ownerName, companyHistory, culture, interviewFocusAreas;
        Integer employeeCount;
        List<String> supportedRoles;
        
        CompanyData(String name, String website, String hrContact, String hiringManager, String ownerName,
                   Integer employeeCount, String companyHistory, String culture, String interviewFocusAreas,
                   String... roles) {
            this.name = name;
            this.website = website;
            this.hrContact = hrContact;
            this.hiringManager = hiringManager;
            this.ownerName = ownerName;
            this.employeeCount = employeeCount;
            this.companyHistory = companyHistory;
            this.culture = culture;
            this.interviewFocusAreas = interviewFocusAreas;
            this.supportedRoles = Arrays.asList(roles);
        }
    }
}
