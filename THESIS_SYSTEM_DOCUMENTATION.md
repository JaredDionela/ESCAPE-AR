# CHAPTER IV: SYSTEM DOCUMENTATION

## 4.1 System Architecture and Technology Stack

### 4.1.1 Overall System Architecture

**[INSERT FIGURE 4.1: System Architecture Diagram]**
*Create a three-tier architecture diagram showing:*
- *Presentation Layer: Android App (Kotlin/Jetpack Compose) and Web Admin (React/TypeScript)*
- *Application Layer: Business Logic, API Layer, Unity AR Integration*
- *Data Layer: Supabase (PostgreSQL Database, Authentication, Storage, Real-time)*
- *Show arrows indicating data flow between layers*
- *Include cloud infrastructure icons (AWS, Supabase cloud)*

The E.S.C.A.P.E. AR (Enhanced Science Curriculum with Augmented and Practical Education through Augmented Reality) system employs a modern three-tier client-server architecture designed to deliver an immersive educational experience across multiple platforms. The architecture follows a modular, layered approach that separates presentation, business logic, and data management concerns, enabling scalability, maintainability, and platform independence.

At the presentation layer, the system utilizes two distinct client applications: a native Android mobile application built with Kotlin and Jetpack Compose, and a responsive web-based administrative panel developed using React with TypeScript. This dual-client approach addresses the distinct needs of end-users (students) who require mobile accessibility and immersive AR experiences, and administrators (teachers) who benefit from comprehensive data management and analytics tools accessible from desktop environments.

The application layer encompasses the business logic implementation distributed between client-side operations and server-side processing. The mobile application handles local data caching, AR rendering through Unity integration, and offline capability management, while the web panel manages content creation, user management, and analytics computation. This distributed processing model optimizes performance by executing computationally intensive tasks locally while maintaining centralized data consistency.

The data layer leverages Supabase, an open-source Backend-as-a-Service (BaaS) platform built on PostgreSQL, providing robust database management, real-time subscriptions, file storage, and authentication services. This cloud-native architecture ensures data availability, automatic backups, and horizontal scalability without the overhead of managing custom server infrastructure. The system's modular design allows independent scaling of components, facilitating future enhancements such as additional client platforms or expanded feature sets without architectural redesign.

### 4.1.2 Technology Stack Justification

**[INSERT FIGURE 4.2: Technology Stack Diagram]**
*Create a layered stack diagram showing:*
- *Frontend Layer: Kotlin, Jetpack Compose, React, TypeScript, Material-UI*
- *Backend Layer: Supabase, PostgreSQL, RESTful APIs, WebSocket*
- *AR/Media Layer: Unity, AR Foundation, ARCore, YouTube Player API*
- *Infrastructure Layer: Google Play Store, Vercel, AWS S3, SSL/TLS*
- *Use icons for each technology with brief labels*

**Frontend Technologies**

The selection of Kotlin as the primary development language for the Android application was driven by its modern language features, null safety guarantees, and official support from Google as the preferred language for Android development. Kotlin's concise syntax and powerful coroutines-based asynchronous programming model enable efficient handling of network operations, database queries, and UI updates without callback complexity. The integration with Jetpack Compose, Android's modern declarative UI framework, provides a reactive programming model that simplifies state management and reduces boilerplate code compared to traditional XML-based layouts.

For the administrative web panel, React with TypeScript was chosen to leverage its component-based architecture, extensive ecosystem, and strong typing capabilities. TypeScript's static type checking prevents runtime errors and improves code maintainability, particularly important in a collaborative development environment. Material-UI (MUI) components provide a consistent, professional design language following Material Design principles, ensuring visual coherence with the Android application and reducing development time through pre-built, accessible components.

**Backend and Database Technologies**

Supabase was selected as the backend platform based on several critical factors: instant RESTful API generation from PostgreSQL schema, built-in authentication with JWT token management, real-time data synchronization through WebSocket subscriptions, and integrated file storage with CDN distribution. Unlike traditional Backend-as-a-Service solutions, Supabase's open-source foundation and PostgreSQL database provide the flexibility of custom SQL queries, stored procedures, and advanced features such as Row Level Security (RLS) policies for granular access control.

PostgreSQL's ACID compliance ensures data integrity during concurrent operations, essential for educational systems where multiple users may interact with quizzes and progress tracking simultaneously. The database's support for JSON data types and full-text search capabilities enables flexible content management for lesson descriptions and quiz questions without rigid schema constraints. Supabase's automatic API generation eliminates the need for manual endpoint creation while maintaining the ability to write custom database functions when specialized logic is required.

**AR and Multimedia Technologies**

Unity integration through Unity as a Library (UaaL) enables AR content delivery within the native Android application without requiring separate application installations. This approach maintains seamless user experience while leveraging Unity's powerful AR Foundation framework, which abstracts platform-specific AR implementations (ARCore for Android). The YouTube Player API integration facilitates video lesson delivery without hosting infrastructure costs, while the library's built-in controls and analytics provide user engagement metrics.

The combination of native Android components for UI and navigation with embedded Unity scenes for AR content represents a hybrid architecture that maximizes the strengths of each platform: Kotlin/Compose for performant, native UI elements and Unity for cross-platform AR content development. This architectural decision also enables potential iOS platform expansion in the future, as Unity AR content remains platform-agnostic while only requiring native UI adaptation.

### 4.1.3 Development Environment and Deployment

The development environment integrates Android Studio for mobile development, Visual Studio Code for web development, and Unity Editor for AR content creation. Git version control with GitHub hosting enables collaborative development, code review workflows, and continuous integration practices. The system employs Gradle for Android build automation and Vite for web application bundling, both configured for development and production optimization profiles.

Deployment architecture utilizes Google Play Store for mobile application distribution, enabling automatic updates and version management. The web administrative panel deploys to Vercel or similar edge network platforms, ensuring global low-latency access for educators worldwide. Supabase's cloud infrastructure handles database hosting, file storage through AWS S3 integration, and API serving with automatic SSL certificate management. This cloud-native deployment strategy eliminates server maintenance overhead while providing enterprise-grade reliability and performance monitoring through built-in analytics dashboards.

---

## 4.2 Development Process

### 4.2.1 Incremental Development Methodology

The E.S.C.A.P.E. AR system was developed following the Incremental Development methodology, a systematic approach where the system was built and delivered in functional increments, each adding new capabilities to the previous version. The development lifecycle was organized into planned iterations, each focusing on delivering complete, testable system components that could be demonstrated and evaluated by stakeholders, including faculty advisors and pilot test users.

Development planning sessions established clear objectives aligned with the project's overall roadmap, breaking down the complete system into logical increments with defined functionality and acceptance criteria. For example, the "Quiz Module" was developed as a complete increment encompassing all quiz-related features: question display, answer validation, score calculation, and teacher question management. This approach ensured that each increment delivered end-to-end functionality rather than partial features, enabling comprehensive testing and stakeholder evaluation at each stage.

Regular progress reviews maintained team coordination and stakeholder alignment by demonstrating completed increments, gathering feedback, and adjusting subsequent increment plans accordingly. The use of project management tools, specifically GitHub Projects with milestone tracking, provided clear visibility into increment completion status and ensured transparency in development progress. After each increment delivery, evaluation sessions identified improvements for both the completed functionality and the development process itself, such as establishing code review guidelines and documentation standards that evolved throughout development.

The Incremental Development approach proved particularly valuable for managing complexity and risk. Initial user research with teachers revealed the need for detailed analytics beyond basic quiz scores, prompting the addition of an Analytics increment to the development plan. The incremental model allowed these enhancements to be incorporated as a distinct, well-defined increment without disrupting previously completed and tested functionality, demonstrating the methodology's flexibility in accommodating evolving requirements while maintaining system stability.

### 4.2.2 Development Phases and Milestones

**[INSERT FIGURE 4.3: Development Timeline/Gantt Chart]**
*Create a timeline or Gantt chart showing:*
- *Phase 1: Requirements Analysis and Design (Weeks 1-3)*
- *Phase 2: Core Module Development (Weeks 4-10)*
- *Phase 3: Integration and Testing (Weeks 11-14)*
- *Phase 4: AR Content Creation (Weeks 15-18)*
- *Phase 5: Deployment and UAT (Weeks 19-20)*
- *Show overlapping activities and key milestones*
- *Highlight deliverables for each phase*

The development process followed five distinct phases: Requirements Analysis and Design, Core Module Development, Integration and Testing, AR Content Creation, and Deployment and User Acceptance Testing.

**Requirements Analysis and Design** (Weeks 1-3) involved stakeholder interviews with science teachers, competitive analysis of existing educational applications, and technical feasibility studies for AR integration. This phase produced comprehensive documentation including use case diagrams, user personas, and initial wireframes. The Entity Relationship Diagram (ERD) was designed during this phase, establishing the database schema foundation that would support all subsequent development.

**Core Module Development** (Weeks 4-10) focused on implementing fundamental system components: user authentication, lesson content management, quiz functionality, and progress tracking. Development followed a feature-driven approach where each major module was completed end-to-end—from database schema to API endpoints to user interface—before moving to the next module. This approach ensured that each completed feature could be independently tested and deployed, reducing integration complexity later.

**Integration and Testing** (Weeks 11-14) concentrated on connecting the mobile application with the web administrative panel through the shared Supabase backend. This phase revealed the importance of consistent data models between platforms, leading to the creation of shared TypeScript interfaces that were manually synchronized with Kotlin data classes. Comprehensive integration tests verified data flow between platforms, such as ensuring that quiz questions created in the web panel appeared correctly formatted in the mobile application.

**AR Content Creation** (Weeks 15-18) involved developing Unity-based AR experiences for each science module (Decantation, Organ System, Simple Machines, Solar System). This phase required close collaboration between developers and content creators to ensure that AR visualizations accurately represented scientific concepts while maintaining performance on mid-range Android devices. Each AR module underwent iterative refinement based on user testing feedback, balancing visual fidelity with educational clarity.

**Deployment and User Acceptance Testing** (Weeks 19-20) encompassed final quality assurance testing, performance optimization, and pilot deployment to a small group of teachers and students. This phase gathered real-world usage data that informed final adjustments to user interface elements, improved error handling, and clarified instructional content. The feedback loop from this phase validated design decisions and identified areas for future enhancement beyond the thesis scope.

### 4.2.3 Continuous Integration and Quality Assurance

Throughout development, continuous integration practices ensured code quality and system stability. Git branching strategies separated feature development (feature branches), stable development code (development branch), and production-ready releases (main branch). Pull requests required peer review before merging, fostering code quality discussions and knowledge sharing among team members.

Automated testing formed a critical quality assurance component, with unit tests covering business logic functions, integration tests verifying API endpoints, and UI tests validating critical user workflows. The Android application employed Espresso for UI testing and JUnit for unit tests, while the web panel utilized React Testing Library and Jest for component and integration testing. Test coverage targets of 70% for critical paths ensured that major functionality remained regression-free as new features were added.

Performance monitoring tools integrated into both applications provided real-time insights into system behavior under various network conditions and device specifications. Crashlytics for Android crash reporting and Sentry for web error tracking enabled proactive issue identification and rapid resolution during development and post-deployment phases. This data-driven approach to quality assurance ensured that the delivered system met reliability and performance standards expected in educational environments.

---

## 4.3 Development Tools

### 4.3.1 Integrated Development Environments (IDEs)

**Android Studio** served as the primary development environment for the mobile application, providing comprehensive support for Kotlin development, Jetpack Compose UI preview, and Android SDK management. The IDE's intelligent code completion, refactoring tools, and integrated debugging capabilities significantly accelerated development productivity. The Layout Inspector and Profiler tools proved invaluable for optimizing UI performance and identifying memory leaks, particularly when integrating Unity AR components which required careful resource management.

Android Studio's built-in emulator facilitated rapid testing across different device configurations and Android API levels without requiring physical device connections. The AVD (Android Virtual Device) manager enabled testing on various screen sizes and resolutions, ensuring responsive design consistency. Additionally, the IDE's Logcat integration provided real-time log filtering and searching, essential for debugging complex interactions between Kotlin code, Compose UI, and Unity-based AR scenes.

**Visual Studio Code** was selected for web development due to its lightweight footprint, extensive extension ecosystem, and superior TypeScript support. Extensions such as ESLint for code linting, Prettier for consistent code formatting, and GitHub Copilot for AI-assisted code completion enhanced development efficiency. The integrated terminal enabled rapid execution of npm scripts for development server launching, production builds, and automated testing without context switching.

The VS Code debugger's integration with Chrome DevTools facilitated sophisticated web application debugging, including breakpoint management, variable inspection, and network request monitoring. React Developer Tools extension provided component hierarchy visualization and state inspection, crucial for debugging complex component interactions in the administrative panel. The editor's multi-cursor editing and powerful find-and-replace capabilities streamlined repetitive refactoring tasks across the large codebase.

**Unity Editor** (Version 2021.3 LTS) was utilized exclusively for AR content creation, providing a visual environment for 3D scene composition, asset management, and AR Foundation configuration. The editor's Play Mode enabled rapid AR testing through device emulation, significantly reducing the iteration cycle for AR content refinement. Unity Package Manager facilitated integration of AR Foundation, ARCore Extensions, and various asset packages while maintaining version control compatibility.

### 4.3.2 Version Control and Collaboration Tools

**Git** and **GitHub** formed the foundation of the project's version control and collaboration infrastructure. The distributed nature of Git enabled independent feature development without network connectivity constraints, while GitHub's cloud hosting provided redundancy and facilitated code sharing with advisors and stakeholders. The repository structure organized code into clearly defined directories for the mobile application (`/app`), web administration panel (`/web-admin`), Unity AR content (`/unity-ar`), and database migrations (`/supabase`).

GitHub's branching and pull request workflows enforced code review processes, ensuring that all code changes underwent peer evaluation before integration into the main codebase. Pull request templates standardized change descriptions, including sections for feature overview, testing performed, and breaking changes, which proved valuable for maintaining project documentation alongside code evolution. GitHub Issues tracked bugs, feature requests, and technical debt, providing a centralized project management interface integrated directly with the codebase.

GitHub Actions automated continuous integration workflows, running automated test suites on every pull request and generating build artifacts for internal testing. Separate workflows handled Android application building (Gradle-based), web application building (Vite-based), and automated deployment to staging environments. This automation reduced manual testing burden and ensured consistent build processes across development environments.

### 4.3.3 Design and Prototyping Tools

**Figma** was employed for user interface design and interactive prototyping, enabling rapid design iteration and stakeholder feedback collection before implementation. The collaborative nature of Figma allowed real-time design discussions with advisors, facilitating consensus on user experience flows and visual design decisions. Design systems established in Figma—including color palettes, typography scales, and component libraries—ensured visual consistency across mobile and web applications.

Interactive prototypes created in Figma simulated user workflows such as quiz taking, lesson viewing, and administrative content management, enabling usability testing with potential users before code implementation. This design-first approach reduced development rework by identifying user experience issues early in the process. Figma's developer handoff features provided CSS specifications and asset exports that directly translated to Material-UI component configurations in the web application.

**Draw.io (diagrams.net)** facilitated creation of technical diagrams including the Entity Relationship Diagram, system architecture diagrams, and application flow charts. The tool's extensive shape libraries for database modeling, UML diagrams, and cloud architecture visualization enabled professional-quality documentation. Diagrams stored as versioned files in the Git repository ensured that technical documentation remained synchronized with code evolution and served as reference materials throughout development.

### 4.3.4 Testing and Quality Assurance Tools

**Supabase Dashboard** provided comprehensive database management, including direct SQL query execution for schema modifications, data inspection for debugging, and real-time log viewing for API request monitoring. The dashboard's policy editor simplified Row Level Security (RLS) configuration, which was critical for implementing secure multi-tenant data access where students only viewed their own progress while teachers accessed aggregated class data.

**Postman** was used extensively for API testing, particularly during backend development phases. Collections of API requests organized by functional area (authentication, lesson management, quiz operations) enabled rapid endpoint testing during development and served as interactive API documentation. Automated test suites within Postman validated API response structures and error handling, catching integration issues before mobile and web client integration.

**Chrome DevTools** and **React Developer Tools** were indispensable for web application debugging and performance optimization. The Network tab monitored API request patterns, identifying opportunities for request batching and caching improvements. The Performance profiler revealed rendering bottlenecks in complex components such as the analytics dashboard with multiple charts, guiding optimization efforts toward memoization and lazy loading strategies.

**Logcat and Firebase Crashlytics** provided comprehensive Android application logging and crash reporting. Logcat's filtering capabilities enabled focused debugging of specific application components during development, while Crashlytics aggregated crash reports from test devices and early users, facilitating rapid issue identification and resolution. The combination of debug-time logging and production crash analytics ensured continuous quality improvement throughout the development lifecycle and beyond initial deployment.

---

## 4.4 Authentication System

### 4.4.1 Authentication Architecture

**[INSERT FIGURE 4.4: Authentication Flow Diagram]**
*Create a sequence diagram or flowchart showing:*
- *User Registration: Email → Supabase Auth → Verification Email → Confirmation*
- *User Login: Email/Password → HTTPS → Supabase Auth → JWT Access Token + Refresh Token*
- *Token Storage: EncryptedSharedPreferences (Android) / HttpOnly Cookies (Web)*
- *Token Refresh: Expired Token → Refresh Token → New Access Token*
- *Authorization: API Request with JWT → Row Level Security Check → Data Access*
- *Show decision points and security checkpoints*

The E.S.C.A.P.E. AR system implements a secure, token-based authentication architecture leveraging Supabase Auth, a production-ready authentication service built on PostgreSQL and GoTrue. The authentication flow employs JSON Web Tokens (JWT) for stateless session management, enabling efficient authentication across both mobile and web platforms without server-side session storage overhead. This approach aligns with modern authentication best practices while maintaining compatibility with the system's distributed architecture.

User authentication begins with email-based registration requiring unique email addresses and secure passwords meeting minimum complexity requirements (minimum 8 characters, including uppercase, lowercase, and numeric characters). Upon successful registration, Supabase Auth generates a verification email containing a secure token link, implementing email verification to prevent fake account creation and ensure communication channel validity. This verification step, while optional for development environments, provides an essential security layer for production deployment.

The login process authenticates users through email and password credentials submitted via HTTPS-encrypted connections, preventing credential interception through man-in-the-middle attacks. Successful authentication returns a JWT access token (valid for 1 hour) and a refresh token (valid for 30 days), stored securely in the Android application's EncryptedSharedPreferences and the web browser's HttpOnly cookies. The access token, included in all subsequent API requests via Authorization headers, enables stateless user identification and authorization without database queries on every request.

Token refresh mechanisms ensure seamless user experience by automatically renewing expired access tokens using stored refresh tokens before they expire. The Supabase client libraries handle this process transparently, maintaining user sessions without requiring re-authentication unless refresh tokens expire due to extended inactivity (30 days). This balance between security and usability ensures that active users remain authenticated while inactive sessions naturally expire, reducing the risk of unauthorized access from abandoned sessions.

### 4.4.2 Role-Based Access Control

**[INSERT FIGURE 4.5: Role-Based Access Control Matrix]**
*Create a table/matrix showing:*
- *Rows: Features (View Lessons, Take Quiz, Create Lessons, Manage Users, View Analytics, etc.)*
- *Columns: Roles (Student, Teacher)*
- *Cells: Checkmarks (✓) for allowed, X marks (✗) for restricted*
- *Include RLS policy indicators for database-level enforcement*
- *Show application-level vs database-level authorization layers*

The authentication system implements role-based access control (RBAC) through a custom `role` field in the user profiles table, distinguishing between `student` and `teacher` roles. Role assignment occurs during registration based on user-selected account type, with the system automatically setting default permissions appropriate to each role. This role differentiation enables the presentation of distinct user interfaces and feature sets: students access learning content and personal progress tracking, while teachers access content management tools and class-wide analytics.

Authorization enforcement occurs at multiple levels to ensure comprehensive security. Database Row Level Security (RLS) policies restrict data access based on authenticated user roles and ownership relationships. For example, students can only read their own progress records (`user_id = auth.uid()`), while teachers can read all progress records for reporting purposes. This database-level enforcement provides defense-in-depth protection, preventing authorization bypasses even if application-level checks fail.

Application-level authorization in the mobile app and web panel verify user roles before rendering role-specific UI components and enabling privileged operations. The mobile application conditionally displays teacher-only features such as the "Create Lesson" option based on the authenticated user's role, while the web administrative panel restricts access entirely to users with teacher roles. These checks prevent UI confusion and unauthorized action attempts while the RLS policies provide ultimate authorization enforcement.

### 4.4.3 Security Measures and Data Protection

Password security employs industry-standard bcrypt hashing with a work factor of 10, ensuring that even in the event of database compromise, password recovery through brute force attacks remains computationally infeasible. Supabase Auth handles password hashing automatically, with passwords never stored in plaintext or transmitted except during initial registration and authentication over encrypted connections. Password reset functionality provides secure account recovery through email-based token verification, enabling users to regain access without administrator intervention.

Session security is enhanced through JWT token expiration policies and secure storage practices. Access tokens' short lifetime (1 hour) limits the window of opportunity for token theft exploitation, while refresh token rotation on renewal prevents replay attacks using stolen tokens. The Android application's use of EncryptedSharedPreferences ensures that tokens remain encrypted at rest using Android's Keystore system, protecting against unauthorized access by malicious applications. Similarly, HttpOnly cookie flags in the web application prevent JavaScript-based token theft through XSS attacks.

Data transmission security relies on HTTPS/TLS encryption for all network communications between clients and the Supabase backend. The SSL/TLS certificates, automatically managed by Supabase's infrastructure, ensure end-to-end encryption of authentication credentials, user data, and API requests. This encryption prevents eavesdropping and tampering attacks on network traffic, essential for protecting sensitive educational data and user credentials, particularly when users connect from public Wi-Fi networks in educational institutions.

API rate limiting and abuse prevention mechanisms protect against brute force authentication attacks and denial-of-service attempts. Supabase Auth implements progressive delays after failed login attempts from the same IP address, making credential stuffing attacks impractical. Additionally, CAPTCHA integration (configurable in Supabase settings) can be enabled to prevent automated bot registration and authentication attempts. These measures collectively ensure system availability and security without compromising legitimate user experience.

### 4.4.4 Privacy and Compliance Considerations

The authentication system design prioritizes user privacy through data minimization principles, collecting only essential information (email, display name, optional teacher/section for students) necessary for system functionality. No personally identifiable information beyond email addresses is required for core system operation, and the system provides mechanisms for users to update or delete their profiles, supporting data subject rights under privacy regulations such as GDPR and local data privacy laws.

Audit logging tracks authentication events including login attempts, password changes, and account modifications, providing transparency and accountability essential for educational institution compliance requirements. These logs, accessible to system administrators through Supabase's dashboard, enable investigation of security incidents and compliance with institutional policies requiring user activity monitoring. However, audit logs are retained with defined expiration policies to balance security needs with privacy considerations.

The segregation of authentication infrastructure (Supabase Auth) from application data storage provides architectural benefits for privacy compliance. User credentials remain isolated in Supabase's secure authentication database, while educational data (lessons, quizzes, progress) resides in the application database with references to user IDs rather than embedding sensitive authentication information. This separation simplifies compliance with data protection regulations that impose stricter controls on authentication credentials compared to general application data.

---

## 4.5 System Pages and Modules

### 4.5.1 Mobile Application - Student Interface

**[INSERT FIGURE 4.6: Mobile Application Interface Screens]**
*Create a multi-panel screenshot or mockup showing:*
- *Panel A: Home Screen with module cards and progress indicators*
- *Panel B: Modules Screen with color-coded science topics*
- *Panel C: Lessons Screen with embedded YouTube player*
- *Panel D: Quiz Screen with multiple-choice questions*
- *Panel E: AR Experience Screen with 3D model overlay*
- *Panel F: Profile Screen with achievements and statistics*
- *Label each screen and highlight key UI components*

**Home Screen (Dashboard)**

The Home Screen serves as the primary navigation hub for students, presenting a personalized overview of their learning journey with immediate access to core functionality. The screen employs a card-based layout following Material Design principles, with each card representing a major system feature: Modules, Lessons, Profile, and AR Experiences. This design reduces cognitive load by organizing complex functionality into visually distinct, easily identifiable sections while maintaining a clean, uncluttered interface suitable for diverse age groups and technical proficiency levels.

The modules card displays progress indicators for each of the four science topics (Decantation, Organ System, Simple Machines, Solar System), using circular progress bars and completion percentages to provide at-a-glance learning status visualization. This gamification element motivates continued engagement by making progress tangible and celebrating incremental achievements. The visual hierarchy emphasizes incomplete modules, directing student attention toward next learning objectives while maintaining visibility of completed content for review purposes.

Personalization enhances user experience through dynamic welcome messages addressing students by their display names and contextual recommendations such as "Continue Learning" buttons that navigate directly to the most recently accessed lesson. This reduces friction in returning to interrupted learning sessions and demonstrates system intelligence in tracking user context. Responsive design ensures optimal viewing across device sizes from compact 5-inch smartphones to larger 10-inch tablets, adapting card layouts and text sizes for readability without sacrificing information density.

**Modules Screen**

The Modules Screen presents the four core science topics as immersive, visually distinct cards with custom illustrations representing each subject matter: laboratory glassware for Decantation, anatomical heart for Organ System, gear mechanisms for Simple Machines, and planetary orbit for Solar System. Each card displays aggregated progress metrics including lesson completion count (e.g., "2/5 lessons completed") and quiz performance (best score percentage), providing motivation through progress transparency while enabling students to identify areas requiring additional study.

Tapping a module card navigates to the Module Detail Screen, which expands to show a vertical timeline of lessons and associated quizzes. This sequential presentation reinforces the curriculum's structured learning path, where each lesson builds upon previous content. Locked content indicators for advanced lessons implement prerequisite enforcement, ensuring students complete foundational material before accessing complex topics—a pedagogical decision aligned with constructivist learning theory principles that emphasize scaffolded knowledge construction.

The module color-coding system (green for Decantation, red for Organ System, orange for Simple Machines, purple for Solar System) extends throughout the application interface, providing consistent visual cues that improve navigation efficiency and reduce cognitive load. This color psychology application associates specific hues with content categories, facilitating mental model formation and improving information recall according to dual-coding theory principles that combine verbal and visual information processing channels.

**Lessons Screen**

The Lessons Screen integrates video-based instruction through embedded YouTube player components, enabling rich multimedia content delivery without infrastructure costs for video hosting and transcoding. The player interface provides standard controls (play/pause, seek, fullback playback speed adjustment) supplemented by platform-specific features such as Picture-in-Picture mode for multitasking and background audio for learners preferring audio-only instruction during activities incompatible with video attention.

Video progress tracking persists playback positions server-side, enabling cross-device learning continuity where students begin lessons on mobile devices during commutes and resume on tablets at home. This synchronization, implemented through Supabase real-time subscriptions, updates progress indicators within seconds of playback events, demonstrating responsive system behavior that builds user trust. Completion criteria require 90% video viewing to mark lessons complete, preventing premature progression while accommodating accidental seeks and repetition of difficult concepts.

Supplementary materials appear below video content in expandable sections, including lesson summaries, downloadable PDF resources, and external reference links to authoritative science education resources. This multimodal content presentation accommodates diverse learning preferences identified in VARK learning style research (Visual, Auditory, Reading/Writing, Kinesthetic), where some students benefit from video instruction while others prefer text-based summaries or hands-on AR activities. The vertical scrolling layout enables efficient content scanning while maintaining focus on the primary video content through strategic whitespace and typography hierarchy.

**Quiz Screen**

The Quiz Screen implements a multi-step form interface presenting one question per screen to minimize visual distraction and cognitive overload during assessment. Each question displays in a card format with large, readable typography (18sp body text, 16sp options) optimized for mobile viewing distances. Multiple-choice options appear as distinct Material Design radio button cards with entire card regions acting as tap targets, improving selection accuracy compared to small radio circles particularly beneficial for users with motor control challenges.

Real-time feedback provides immediate positive or negative reinforcement upon answer selection through color-coded indicators (green for correct, red for incorrect) and optional explanatory text elaborating on correct answers. This formative assessment approach, grounded in cognitive science research on spaced repetition and immediate feedback, enhances learning retention compared to summative assessments that delay feedback until completion. The system records all responses including incorrect attempts, enabling analytics features that identify commonly misunderstood concepts requiring instructional attention.

Quiz navigation provides review and correction capabilities through "Previous" and "Next" buttons, allowing students to reconsider answers before final submission. A progress indicator displaying "Question 5 of 10" maintains orientation within the assessment while a floating "Submit Quiz" button appears only after all questions receive responses, preventing accidental premature submission. Upon completion, a results screen presents overall score, correct/incorrect question breakdown, and recommendation to retry if performance falls below 70% mastery threshold, encouraging growth mindset attitudes toward learning from mistakes.

**AR Experience Screen**

The AR Experience Screen activates device cameras and overlays computer-generated 3D models onto physical environments through ARCore integration, providing immersive visualizations of abstract scientific concepts difficult to represent through static images or video. Each science module includes dedicated AR experiences: molecular separation visualization for Decantation, interactive anatomical models for Organ System, mechanical animation demonstrations for Simple Machines, and scale models of celestial bodies for Solar System.

AR content initialization requires environmental scanning to detect horizontal or vertical surfaces for model placement, guided by on-screen instructions such as "Move your phone slowly to scan the area." This onboarding flow accommodates first-time AR users while maintaining efficiency for experienced users through skip options after successful surface detection. Placement reticles provide visual feedback during scanning, building anticipation and demonstrating system responsiveness essential for maintaining user engagement during initialization delays.

Interactive manipulation capabilities enable students to rotate, scale, and reposition 3D models through intuitive touch gestures (pinch to zoom, drag to rotate, two-finger pan to reposition), promoting active learning through kinesthetic engagement. Annotation overlays appear contextually when users focus on specific model components, providing information labels and explanatory text without cluttering the visual field during spatial exploration. Screenshot functionality captures AR experiences for inclusion in learning portfolios or sharing with peers, extending learning beyond application sessions into social and reflective domains.

**Profile Screen**

The Profile Screen consolidates personal information, learning statistics, and achievement tracking into a comprehensive user identity representation. The top section displays user avatars (customizable through photo upload or default icon), display names, and metadata such as teacher names and section assignments for classroom organizational purposes. This personalization fosters ownership of learning progress while supporting teacher identification of students within the management interface.

Achievement badges visualize learning milestones through progressive unlocking mechanics: "First Steps" for completing an initial module, "Knowledge Seeker" for two completions, "Halfway There" for 50% completion, "Perfect Score" for 100% quiz performance, "Outstanding Student" for 90%+ average, and "Escape Master" for full curriculum completion. These gamification elements leverage intrinsic motivation principles by providing concrete, attainable goals that scaffold toward ultimate mastery, with visual distinction (gold highlighting) for the ultimate achievement creating aspirational targets.

Progress statistics present quantitative learning metrics including total modules completed, average quiz scores, and per-module performance breakdowns. Data visualization through circular progress indicators and horizontal bar charts translates abstract numbers into intuitive visual representations accessible to users with varying numerical literacy levels. The inclusion of comparative historical data (e.g., "Your score improved 15% from last attempt") provides formative feedback that encourages growth mindset development and persistence through challenges, both critical factors in self-directed learning success.

### 4.5.2 Web Application - Teacher/Admin Interface

**[INSERT FIGURE 4.7: Web Admin Interface Screens]**
*Create a multi-panel screenshot or mockup showing:*
- *Panel A: Admin Dashboard with KPI cards and charts*
- *Panel B: Lessons Management table with CRUD operations*
- *Panel C: Quiz Question Management with question editor*
- *Panel D: User Management with student profiles*
- *Panel E: Analytics Dashboard with performance graphs*
- *Show navigation sidebar on the left of each panel*

**Admin Dashboard**

The Admin Dashboard serves as the primary landing page for educators, presenting key performance indicators (KPIs) through a card-based metric display showing quiz completion rates, average student scores, students requiring intervention, and top-performing students. This executive summary design enables rapid assessment scanning during brief login sessions while providing drill-down capabilities for detailed investigation. Real-time data synchronization through Supabase subscriptions ensures dashboard metrics reflect current system state, critical for time-sensitive interventions such as identifying struggling students before assignment deadlines.

The "Students Needing Help" metric applies business logic to identify learners scoring below 50% on quizzes or showing declining performance trends, flagging at-risk students for proactive teacher outreach. This early warning system operationalizes data-driven instruction practices by surfacing actionable insights from raw performance data that might otherwise remain hidden in detailed reports. Color-coded performance indicators (green for above-target, yellow for borderline, red for concerning) leverage universal color associations to communicate urgency without requiring numerical interpretation.

Recent activity feeds display chronological lists of system events including new student registrations, quiz completions, and lesson accesses, providing transparency into platform usage patterns. Timestamp formatting ("2 hours ago," "Yesterday") improves temporal orientation compared to absolute dates, while user avatars and color-coded event type icons enhance scanability. This activity monitoring supports responsive teaching where educators acknowledge student efforts promptly, reinforcing engagement through timely recognition particularly valuable in asynchronous learning environments.

**Lessons Management**

The Lessons Management interface implements a complete CRUD (Create, Read, Update, Delete) system for educational content administration through a table-based layout displaying existing lessons with sortable columns for module, title, order, and action buttons. This enterprise software pattern provides efficiency for managing dozens or hundreds of content items while maintaining clarity through pagination and search/filter capabilities. Batch operations enable simultaneous updates to multiple lessons, reducing repetitive tasks when reorganizing curriculum sequences.

The lesson creation form implements progressive disclosure UI patterns, presenting essential fields (module selection, title, YouTube video ID) prominently while collapsing optional metadata (thumbnail URL, custom descriptions) into expandable sections. This reduces form intimidation for novice users while preserving power user efficiency through keyboard shortcuts and field auto-population based on historical patterns. Intelligent YouTube ID extraction recognizes various URL formats (full URLs, shortened links, embed codes), automatically parsing video identifiers and eliminating manual ID extraction—a subtle but impactful usability enhancement reducing error-prone copy-paste operations.

Real-time preview panels display video thumbnails and playback controls as teachers input YouTube IDs, providing immediate validation that selected videos meet instructional objectives before form submission. This preview-before-commit pattern prevents content publication errors that would require subsequent corrections, particularly important when coordinating lesson releases with classroom schedules. File attachment capabilities support supplementary material uploads (PDFs, presentations, worksheets) with drag-and-drop interfaces that modernize traditional file input elements while maintaining fallback compatibility for keyboard navigation.

**Quiz Question Management**

The Quiz Management system organizes questions hierarchically by science modules with dedicated question banks for each subject area, preventing cross-module question contamination while enabling teachers to build comprehensive assessments aligned with specific learning objectives. Module-specific question lists display existing items in examination format, showing question text, correct answers, and rapid action buttons for editing, duplication (creating similar questions with variations), and deletion.

The question editor employs a form-based interface with text area inputs for question stems and four option fields, with radio button selection clearly indicating correct answers through visual highlighting. This explicit visual confirmation prevents the common error of forgetting to designate correct answers, which would invalidate assessments. Character counters below text inputs guide appropriate question length, balancing sufficient detail for clarity with conciseness for mobile display constraints—a critical consideration often overlooked in desktop-first educational software design.

Question ordering controls enable drag-and-drop resequencing for logical progression that builds from foundational concepts to advanced applications, aligning with Bloom's Taxonomy levels (Remember → Understand → Apply → Analyze). Auto-save functionality persists changes after brief input pauses, preventing data loss from inadvertent tab closures while providing visual save status indicators that build user confidence in data persistence. Export capabilities generate question banks in standardized formats compatible with learning management systems, supporting institutional workflows that require content backup or multi-platform distribution.

**User Management**

The User Management interface presents comprehensive student profiles through tabular displays with search, filtering, and sorting capabilities essential for managing classes of 20-50 students. Each row displays key identifiers (avatar, name, email) alongside role indicators (student/teacher) and registration dates, with quick access action buttons for user operations. This streamlined interface accommodates different teacher workflow needs from quick attendance verification to comprehensive learner portfolio review.

**User Registration and Creation**

Teachers can directly register new users through the administrative panel, streamlining the onboarding process for students who may face difficulties with self-registration. The user creation form collects essential information including email address, display name, initial password, role assignment (student/teacher), and optional metadata such as assigned teacher name and section. This administrative registration capability proves particularly valuable during bulk student enrollment at semester start, enabling teachers to prepare accounts before class sessions begin. The system validates email uniqueness and password complexity requirements, providing immediate feedback on constraint violations to prevent registration errors.

**User Profile Management**

Individual user detail views present comprehensive learning analytics specific to each student: quiz performance trends across modules, module completion status, and achievement badge progress. These profiles enable personalized instruction planning by identifying each learner's strengths and areas requiring additional support. Teachers can access detailed performance breakdowns showing quiz attempt history, best scores per module, and completion timestamps, providing evidence-based insights for academic interventions. Comparative visualizations, such as student performance plotted against class averages, provide context for interpreting individual metrics—a critical feature since absolute scores lack meaning without benchmark comparison.

The profile editing functionality enables teachers to update student information including display names, assigned teacher names, and section assignments—essential for accommodating classroom roster changes throughout the academic year. Password reset capabilities allow administrators to assist students who have forgotten credentials by generating secure temporary passwords sent via email verification. However, certain immutable fields such as email addresses and user roles cannot be modified after account creation to maintain data integrity and prevent unauthorized privilege escalation, requiring account deletion and recreation if fundamental changes are necessary.

**User Deletion and Data Management**

The user deletion process implements comprehensive data removal following CASCADE delete policies established at the database level. When a teacher deletes a user account, the system automatically removes all associated data including quiz results, progress records, lesson progress tracking, and achievement data—ensuring complete removal of personally identifiable information and compliance with data protection regulations. This cascading deletion prevents orphaned records that would corrupt analytics calculations while supporting institutional right-to-erasure requirements under privacy laws such as GDPR.

Critically, user deletion also releases the associated email address for reuse, enabling the same email to be registered again if a student re-enrolls after withdrawal or if an email was registered in error. The deletion confirmation dialog explicitly warns administrators of the permanent, irreversible nature of this operation with clear messaging: "Deleting this user will permanently erase all progress data, quiz results, and achievements. The email address will be available for re-registration. This action cannot be undone." This defensive design pattern acknowledges human error potential in administrative interfaces with significant consequences, requiring explicit confirmation before executing destructive operations.

Export functionality generates CSV/Excel reports for institutional record-keeping requirements or integration with school information systems, recognizing that educational technology rarely exists in isolation but must interoperate with established administrative infrastructure. Bulk operations support efficient classroom management tasks including section reassignments when classroom rosters change and cohort-based filtering for targeted analytics review.

**Analytics Dashboard**

The Analytics Dashboard synthesizes learning data into actionable insights through interactive visualizations including line charts tracking performance trends over time, bar charts comparing module difficulty (inferred from average scores), and pie charts showing student engagement distribution. Chart.js library integration provides responsive, accessible visualizations that adapt to various screen sizes while maintaining data integrity and readability, essential for educators reviewing analytics on diverse devices from desktop monitors to tablets.

Module-level performance breakdowns identify curriculum areas where students consistently struggle, indicated by below-threshold average scores or high attempt counts, signaling potential instructional design issues requiring content revision or supplementary resource creation. Conversely, modules with exceptionally high performance may indicate overly simple content requiring enrichment activities for advanced learners. These diagnostic insights transform raw assessment data into curriculum quality feedback loops, operationalizing continuous improvement processes often limited to anecdotal observations in traditional instruction.

Longitudinal trend analysis reveals learning progression patterns, distinguishing between momentary comprehension dips (recoverable through review) and sustained performance decline (requiring intervention). Exportable reports with date range selection enable evidence-based parent-teacher conferences and formal academic evaluations, providing concrete data to support qualitative observations. The inclusion of statistical measures such as standard deviation highlights performance variability within classes, revealing whether low average scores result from universal struggle (instructional issue) or specific students requiring targeted support (differentiation opportunity).

---

## 4.6 System Diagrams and Data Flow

### 4.6.1 Entity Relationship Diagram (ERD)

**[INSERT FIGURE 4.8: Entity Relationship Diagram]**
*Create a comprehensive ERD showing:*

**Core Entities (Primary Tables):**
- **profiles**: id (PK), email (UNIQUE), display_name, role (ENUM: student/teacher), teacher_name, section, avatar_url, created_at
- **lessons**: id (PK), module_id (FK), title, description, youtube_video_id, thumbnail_url, duration, order_index, created_at
- **quiz_questions**: id (PK), module_id (FK), question_text, option_a, option_b, option_c, option_d, correct_answer (ENUM: A/B/C/D), order_index

**Progress Tracking Entities:**
- **quiz_results**: id (PK), user_id (FK→profiles), question_id (FK→quiz_questions), selected_answer, is_correct, created_at
- **progress**: id (PK), user_id (FK→profiles), module (ENUM), best_score, completed (BOOLEAN), attempt_count, UNIQUE(user_id, module)

**Relationships (Cardinality):**
- profiles ←(1:N)→ quiz_results  
- profiles ←(1:N)→ progress
- quiz_questions ←(1:N)→ quiz_results
- modules ←(1:N)→ lessons
- modules ←(1:N)→ quiz_questions

*Use standard ERD notation with crow's foot symbols for relationships*
*Highlight primary keys (PK), foreign keys (FK), and unique constraints*
*Show ON DELETE CASCADE for user-related relationships (quiz_results, progress)*

The Entity Relationship Diagram represents the logical data model underlying the E.S.C.A.P.E. AR system, defining entities (tables), attributes (columns), and relationships (foreign keys) that collectively organize and preserve educational data integrity. The ERD design follows third normal form (3NF) principles to eliminate data redundancy while maintaining referential integrity through carefully designed relationship constraints.

**Core Entities and Attributes**

The `profiles` entity serves as the central user representation, storing essential identity information including unique identifiers (UUID primary keys), display names, email addresses (unique constraint enforcing one account per email), role designations (student/teacher enum), and optional metadata (teacher names, section assignments, avatar URLs). The separation of authentication credentials (managed by Supabase Auth) from profile data exemplifies security best practices where password hashes remain isolated from application data, limiting exposure in case of data breaches.

The `lessons` entity encapsulates educational content metadata including module associations (foreign key to modules), titles, descriptions, YouTube video identifiers, thumbnail URLs, duration estimates, and ordering indices. The ordering index enables curriculum sequencing control, critical for prerequisite-based learning paths where advanced concepts depend on foundational understanding. The relationship between lessons and modules implements a many-to-one cardinality where each lesson belongs to exactly one module, but modules contain multiple lessons, reflecting the hierarchical structure of scientific curricula.

The `quiz_questions` entity stores assessment items with question text, four multiple-choice options (option_a through option_d), correct answer indicators (enum constrained to A/B/C/D values preventing invalid data entry), and ordering indices for consistent question sequencing across student attempts. The foreign key relationship to modules enables module-specific question banks, preventing questions about organ systems from appearing in solar system quizzes—a constraint enforcing logical data organization that prevents administrative errors.

**Transactional and Progress Tracking Entities**

The `quiz_results` entity records individual question responses with user identifiers, question identifiers, selected answers, correctness flags, and submission timestamps. This event-sourcing approach preserves complete assessment history, enabling sophisticated analytics such as question difficulty analysis (percentage of students answering correctly), common wrong answer identification (revealing misconceptions), and student learning trajectory visualization (comparing performance across attempts). The decision to store each response rather than summary scores provides flexibility for future analytics requirements unforeseen during initial design.

The `progress` entity maintains aggregate performance metrics per student-module combination, storing best scores (highest achieved percentage), completion flags, and attempt counts. This denormalized summary table optimizes query performance for common operations like dashboard metric retrieval, which would otherwise require expensive aggregation queries across thousands of detailed quiz_results records. The trade-off between data redundancy (calculated fields derived from quiz_results) and query performance exemplifies database design pragmatism where pure normalization yields to performance requirements in read-heavy workloads.

The system intentionally omits granular lesson video progress tracking (such as last watched timestamps or playback positions) to simplify the data model and reduce storage overhead. Students access video content through YouTube's embedded player which maintains its own playback history at the platform level, eliminating redundant tracking in the application database. This architectural decision reduces database write operations during video viewing sessions while leveraging YouTube's robust content delivery infrastructure for optimal streaming performance.

**Relationship Integrity and Constraints**

Foreign key constraints enforce referential integrity between entities: quiz_results must reference valid user_id from profiles and question_id from quiz_questions, preventing orphaned records that would corrupt data analysis. The ON DELETE CASCADE modifier for user-related relationships ensures that deleting a user account automatically removes all associated quiz results, progress records, and achievement data, maintaining database consistency without requiring manual cleanup scripts. This cascading deletion policy supports institutional data protection compliance by enabling complete user data removal when required, while also releasing email addresses for potential reuse in future registrations.

Check constraints enforce business rules at the database level: score_percentage must range between 0-100, module names must match enumerated values (decantation, organ_system, simple_machines, solar_system), and created_at timestamps cannot exceed current time (preventing temporal anomalies from time zone misconfigurations). These database-enforced constraints provide defense-in-depth validation complementing application-level validation, essential when multiple clients (mobile app, web admin, future integrations) access shared data.

Unique constraints prevent data duplication errors: email addresses must be unique across profiles (preventing account conflicts), and (user_id, module) combinations must be unique in progress aggregations (ensuring single summary record per student-module). These constraints transform potential runtime errors into immediate database-level rejections with clear error messages guiding developers toward correct data access patterns. The email uniqueness constraint operates at the database level, automatically enforced even after user deletion, ensuring email availability for re-registration without additional application logic.

### 4.6.2 Mobile Application Flow Diagram

**[INSERT FIGURE 4.9: Mobile Application Navigation Flow]**
*Create a comprehensive flowchart showing:*

**Main Navigation Flow:**
1. **App Launch** → Splash Screen → Check Authentication
   - If Authenticated → Home Screen
   - If Not Authenticated → Login Screen

2. **Authentication Flow:**
   - Login Screen ⇄ Registration Screen
   - Registration → Email Verification → Login
   - Login Success → Home Screen

3. **Student Learning Flow:**
   - Home Screen → Modules Screen → Module Detail
   - Module Detail → Lesson Screen → Video Player
   - Lesson Complete → Quiz Screen → Question 1→2→3...→N
   - Quiz Submit → Results Screen → Back to Module Detail

4. **AR Experience Flow:**
   - Module Detail → AR Experience → Surface Detection
   - Surface Detection → Model Placement → Interaction Mode
   - Capture Screenshot / Exit → Back to Module

5. **Profile & Progress Flow:**
   - Home Screen → Profile Screen
   - View Statistics / View Achievements / Edit Profile

*Use different colors for different user roles (Student=Blue, Teacher=Green)*
*Show decision diamonds for conditional navigation*
*Indicate back navigation with dashed arrows*

The Mobile Application Flow Diagram visualizes user journeys through the Android application from launch to specific goal completions, identifying decision points, system interactions, and navigation paths that collectively define user experience.

**Authentication and Onboarding Flow**

The application flow begins with a splash screen displaying brand identity while performing initialization tasks (checking authentication status, preloading critical resources). Authenticated users proceed directly to the home screen (providing efficient re-entry experience), while unauthenticated users route to the login screen with alternative paths to registration screens. This conditional routing implements session persistence where users remain logged in until explicit logout or token expiration, balancing security with convenience.

The registration flow implements progressive disclosure through multi-step forms collecting essential information incrementally (email/password → display name → role selection → optional profile details). This staged approach reduces form abandonment rates compared to single-page forms with numerous fields, as completion appears more achievable when progress is visible. Server-side validation provides real-time feedback on username availability and password strength, preventing submission of invalid data that would generate error messages requiring form correction and resubmission.

Role-based navigation divergence occurs post-authentication where teachers access administrative features through an alternate home screen layout emphasizing content management over content consumption. This architectural decision to maintain separate navigation flows rather than conditionally hiding features simplifies UI logic while creating distinct, optimized experiences for each user type. State management through Jetpack Compose's viewModel architecture ensures that navigation state persists across configuration changes (screen rotation, theme switching) without losing user context.

**Learning Content Flow**

The primary student learning flow proceeds from home screen → module selection → lesson list → video viewing → quiz attempt → results review → return to module list. This cyclical pattern supports iterative learning where students repeat module content to improve understanding and scores. Strategic placement of "Continue Learning" shortcuts at multiple entry points reduces navigation clicks for common workflows, implementing "zero-click" interface design principles that minimize steps to high-frequency actions.

Lesson viewing implements a vertical content consumption flow where students scroll through integrated components: video player → lesson summary → supplementary materials → quiz launch button. This sequential layout guides attention naturally from content delivery to comprehension verification, leveraging reading direction conventions (top-to-bottom, left-to-right in Western contexts). Auto-play resistance prevents automatic quiz initiation, respecting user agency in determining readiness for assessment—a design decision supporting self-paced learning philosophy.

Quiz flows implement a linear sequence preventing backward navigation after question submission to discourage pattern gaming where students cycle through questions seeking answers before serious attempts. However, review mode (post-submission) enables backward navigation through completed assessments, supporting reflective learning where students analyze incorrect responses to understand mistakes. This dual-mode approach balances assessment integrity during active attempts with educational value during review sessions.

**AR Experience Flow**

AR content activation follows a distinct flow branch from module screens through AR readiness checks (camera permissions, ARCore availability, sufficient lighting conditions) to surface detection → model placement → interactive exploration. Each stage includes instructional overlays guiding first-time users through unfamiliar AR interactions while providing skip options for experienced users, balancing onboarding thoroughness with efficiency.

The AR experience operates in a modal context (full-screen, minimal UI) emphasizing immersive content exploration with discreet exit mechanisms (back button, dedicated close icon) preventing accidental exits from gesture misinterpretation. Persistent instruction panels provide context-sensitive help without cluttering the AR viewport, appearing automatically during inactivity periods and dismissing upon interaction resumption. This dynamic help system accommodates the wide range of AR familiarity among users from completely novice to enthusiast.

### 4.6.3 Web Application Flow Diagram

**[INSERT FIGURE 4.10: Web Application Navigation Flow]**
*Create a comprehensive flowchart showing:*

**Main Navigation Flow:**
1. **Login** → Authentication Check → Admin Dashboard

2. **Content Management Flow:**
   - Dashboard → Lessons Management → View Lessons Table
   - Add New Lesson → Form Entry → YouTube Preview → Save/Publish
   - Edit Lesson → Update Fields → Preview → Save Changes
   - Delete Lesson → Confirmation Dialog → Delete Success

3. **Quiz Management Flow:**
   - Dashboard → Quiz Management → Select Module
   - View Question Bank → Add Question → Form Entry
   - Designate Correct Answer → Save → Add Another / Done
   - Edit/Delete Questions → Confirmation → Success

4. **User Management Flow:**
   - Dashboard → User Management → View Users Table
   - Search/Filter Users → Select User → View Details
   - Edit Profile / Reset Password / Delete User
   - Bulk Operations → Select Multiple → Confirm Action

5. **Analytics Flow:**
   - Dashboard → Analytics Dashboard
   - View Charts → Date Range Selection → Export Report
   - Click Metric → Drill-down to Details → View Specific Students

*Use rectangles for pages, diamonds for decisions, cylinders for database operations*
*Show role-based access restrictions with red X marks*
*Indicate data validation points with checkmark symbols*

The Web Application Flow Diagram maps teacher and administrator navigation patterns through the content management system, highlighting administrative workflows distinct from student learning paths.

**Content Management Workflows**

The primary content creation workflow proceeds from dashboard → lessons management → create lesson form → input lesson details → preview YouTube content → attach supplementary files → publish. This workflow emphasizes validation at each stage (YouTube ID format checking, required field validation, file size limits) before allowing final publication, preventing incomplete or erroneous content from reaching students. Draft saving capabilities enable interrupted workflows where teachers compose content across multiple sessions without losing progress.

The quiz creation workflow follows a similar pattern with module selection → question bank view → create question form → input question text and options → designate correct answer → save and continue. Batch creation workflows enable rapid question development through form duplication with pre-filled common elements (module association, partial text for series questions), addressing teacher feedback during user testing that individual question creation was tedious for comprehensive assessment construction.

Content editing flows preserve workflow continuity by maintaining context when navigating from list views to edit forms and back, using browser history stack for navigation state preservation rather than full page reloads. This single-page application (SPA) architecture provides desktop application responsiveness while maintaining web accessibility, crucial for teacher workflows involving frequent context switching between content management and student monitoring.

**Analytics and Reporting Workflows**

Analytics workflows support both exploratory data analysis (flexible dashboard interactions with date range selection and metric drill-down) and structured reporting (predefined report generation with PDF export). The dashboard serves as an executive summary launching point with contextual links from metrics to detailed views: clicking "Students Needing Help" navigates to filtered user list showing at-risk students, clicking module performance charts navigates to question-level analytics revealing specific items causing difficulty.

Report generation workflows implement asynchronous processing for large data exports, displaying progress indicators during computation and providing download links upon completion rather than blocking the UI during processing. This architectural decision prevents browser timeout issues with large datasets while communicating system responsiveness, critical for maintaining user confidence during operations lacking immediate visual feedback.

**User Management Workflows**

User administration workflows balance bulk operations (class roster imports, batch section reassignments) with individual management (user registration, profile editing, password resets, detailed progress review, and account deletion). The "Add New User" functionality enables teachers to directly register students through a comprehensive form collecting email, display name, initial password, role assignment, and optional teacher/section metadata, streamlining the onboarding process particularly during bulk enrollment periods at semester start.

Profile editing capabilities allow teachers to update student information including display names, assigned teacher names, and section assignments to accommodate classroom roster changes throughout the academic year. However, critical fields such as email addresses and user roles remain immutable after creation to maintain data integrity and prevent unauthorized privilege escalation. Password reset functionality generates secure temporary passwords sent via email verification, enabling students to regain account access without compromising security through administrator-visible passwords.

User deletion operations implement comprehensive data removal policies with explicit confirmation dialogs displaying action summaries: "Deleting this user will permanently erase all progress data, quiz results, and achievements. The email address will be available for re-registration. This action cannot be undone." This defensive design pattern acknowledges human error potential in administrative interfaces with significant consequences. The cascading deletion removes all user-associated records including quiz results, progress tracking, and achievements while releasing the email address for potential reuse, supporting institutional data protection compliance and re-enrollment scenarios. Audit trails log all administrative actions with timestamps and administrator identifiers, providing accountability essential in institutional contexts with multiple staff members sharing system access.

### 4.6.4 Data Flow and Integration Patterns

**[INSERT FIGURE 4.11: System Data Flow Diagram]**
*Create a comprehensive data flow diagram showing:*

**Components:**
- Android App (Student)
- Web Admin (Teacher)
- Supabase API Layer
- PostgreSQL Database
- Authentication Service
- File Storage (AWS S3)
- Unity AR Module

**Data Flows:**
1. **Authentication Flow:**
   - User → Login Request → Supabase Auth → JWT Token → User
   - Token stored in EncryptedSharedPreferences/HttpOnly Cookies

2. **Content Delivery Flow:**
   - Student → GET /lessons → API → Database → JSON Response → App
   - Cached locally in SQLite for offline access

3. **Quiz Submission Flow:**
   - Student → Quiz Answers → POST /quiz_results → Validation → Database
   - Response → Update UI → Update Progress Table

4. **Real-time Sync Flow:**
   - Database Change → PostgreSQL NOTIFY → WebSocket
   - → Subscribed Clients → UI Update (Dashboard metrics update)

5. **Content Management Flow:**
   - Teacher → Create/Edit Content → API → Validation
   - → Database INSERT/UPDATE → Success Response

6. **File Upload Flow:**
   - Teacher → Upload File → Supabase Storage → AWS S3
   - → CDN URL → Database (lesson.attachment_url)

*Use arrows to show data direction*
*Label arrows with request types (GET, POST, PUT, DELETE)*
*Show caching layers and offline storage*
*Indicate real-time WebSocket connections with wavy lines*

The comprehensive system data flow integrates mobile applications, web applications, and cloud backend services through well-defined API contracts and real-time synchronization mechanisms.

**Client-Server Data Exchange**

REST API interactions follow standard HTTP patterns where clients initiate requests to server endpoints receiving JSON-formatted responses. Query operations (GET requests) retrieve data with filtering through URL parameters (e.g., `/lessons?module=decantation`), while mutations (POST/PUT/DELETE) submit data in request bodies with transaction safety through database constraints preventing partial updates during failures. This stateless interaction model simplifies horizontal scaling where load balancers distribute requests across multiple server instances without session affinity requirements.

Real-time data synchronization leverages Supabase's subscription mechanism built on PostgreSQL's LISTEN/NOTIFY features, where clients establish WebSocket connections receiving instant notifications when database tables change. This enables live dashboard updates when students complete quizzes, providing teachers with real-time progress visibility without manual refresh requirements. The subscription architecture implements a pub-sub pattern where database triggers emit events consumed by interested clients, decoupling data producers (student applications) from consumers (teacher dashboards).

Optimistic UI updates in both mobile and web applications immediately reflect user actions locally before server confirmation, preventing the latency-induced sluggishness that diminishes perceived responsiveness. On successful server acknowledgment, optimistic updates persist; on failure, applications revert changes and display error messages. This pattern, common in modern applications from social media to productivity software, manages the tension between responsive interfaces and eventual consistency in distributed systems.

**Offline Capability and Data Synchronization**

The mobile application implements selective offline functionality where lesson content (video URLs, descriptions) and quiz questions cache locally in SQLite databases, enabling continued learning during network disruptions common in educational settings with variable connectivity. Synchronization upon reconnection uploads quiz attempts and progress updates accumulated offline, resolving conflicts through last-write-wins strategies acceptable given the low probability of simultaneous edits to individual user progress from multiple devices.

File attachments (PDFs, supplementary materials) download to device storage with cache expiration policies balancing storage consumption against network efficiency. Popular files accessed by many students cache longer than obscure materials, implementing a least-recently-used (LRU) eviction strategy that maximizes cache hit rates. Progressive loading techniques download low-resolution preview versions immediately while fetching full quality asynchronously, maintaining interface responsiveness during large file operations.

---

## Conclusion

The system documentation presented in this chapter comprehensively details the technical architecture, development process, and functional components of the E.S.C.A.P.E. AR educational platform. The deliberate selection of modern technologies—Kotlin and Jetpack Compose for mobile development, React and TypeScript for web administration, and Supabase for backend services—reflects industry best practices while prioritizing development efficiency and long-term maintainability. The Incremental Development methodology enabled systematic delivery of functional system components through planned iterations, ensuring alignment between technical implementation and educational requirements while managing complexity and risk effectively.

The authentication system's emphasis on security through token-based authentication, role-based access control, and database-level authorization enforcement demonstrates commitment to protecting sensitive educational data and user privacy. The comprehensive suite of system pages, spanning student learning interfaces to teacher administrative tools, illustrates careful user experience design accommodating the distinct needs of different user roles. Finally, the detailed system diagrams—Entity Relationship Diagram, application flow diagrams, and data flow visualizations—provide technical blueprints that facilitate future system enhancements and serve as maintenance documentation for sustained system operation.

The architectural decisions documented herein balance theoretical best practices with practical constraints including development timeline, team expertise, and institutional technical infrastructure requirements. The resulting system demonstrates that modern software engineering methodologies, appropriately adapted to educational technology contexts, can produce robust, scalable platforms that enhance teaching effectiveness and student learning outcomes through thoughtful integration of emerging technologies such as augmented reality and cloud-based learning analytics.
