package com.example.escape_ar.ui.screens

fun getDecantationQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion(
        question = "What is decantation primarily used for?",
        options = listOf(
            "Mixing two liquids together",
            "Separating solid particles from liquid",
            "Heating a liquid to boiling point",
            "Measuring the volume of a liquid"
        ),
        correctAnswer = 1,
        explanation = "Decantation is used to separate solid particles that have settled from a liquid."
    ),
    QuizQuestion(
        question = "Which of the following is the best example of decantation?",
        options = listOf(
            "Filtering coffee grounds",
            "Pouring wine to separate sediment",
            "Boiling water to purify it",
            "Mixing oil and water"
        ),
        correctAnswer = 1,
        explanation = "Pouring wine carefully to leave sediment behind is a classic example of decantation."
    ),
    QuizQuestion(
        question = "What must happen before decantation can be effective?",
        options = listOf(
            "The mixture must be heated",
            "The mixture must be stirred vigorously",
            "The solid particles must settle",
            "The liquid must be cooled"
        ),
        correctAnswer = 2,
        explanation = "Particles must settle to the bottom before the clear liquid can be poured off."
    ),
    QuizQuestion(
        question = "Decantation works best when the solid particles are:",
        options = listOf(
            "Very small and light",
            "Dissolved in the liquid",
            "Large and heavy",
            "The same density as the liquid"
        ),
        correctAnswer = 2,
        explanation = "Large, heavy particles settle faster and more completely, making decantation more effective."
    ),
    QuizQuestion(
        question = "What is a limitation of decantation?",
        options = listOf(
            "It only works with hot liquids",
            "It cannot separate very fine particles completely",
            "It requires expensive equipment",
            "It changes the chemical composition of substances"
        ),
        correctAnswer = 1,
        explanation = "Very fine particles may not settle completely, making perfect separation difficult."
    )
)

fun getOrganSystemQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion(
        question = "Which organ system is responsible for transporting nutrients throughout the body?",
        options = listOf(
            "Nervous system",
            "Respiratory system", 
            "Circulatory system",
            "Digestive system"
        ),
        correctAnswer = 2,
        explanation = "The circulatory system, including the heart and blood vessels, transports nutrients, oxygen, and waste products."
    ),
    QuizQuestion(
        question = "What is the primary function of the respiratory system?",
        options = listOf(
            "Breaking down food",
            "Filtering waste from blood",
            "Exchanging oxygen and carbon dioxide",
            "Producing hormones"
        ),
        correctAnswer = 2,
        explanation = "The respiratory system's main job is gas exchange - taking in oxygen and removing carbon dioxide."
    ),
    QuizQuestion(
        question = "Which organ is the control center of the nervous system?",
        options = listOf(
            "Heart",
            "Brain",
            "Lungs",
            "Liver"
        ),
        correctAnswer = 1,
        explanation = "The brain is the central control organ of the nervous system, processing information and controlling body functions."
    ),
    QuizQuestion(
        question = "The digestive system breaks down food into:",
        options = listOf(
            "Larger molecules for storage",
            "Waste products only",
            "Smaller molecules for absorption",
            "Energy for immediate use"
        ),
        correctAnswer = 2,
        explanation = "Digestion breaks down complex food molecules into smaller, absorbable nutrients."
    ),
    QuizQuestion(
        question = "Which system helps maintain body temperature and protects against infection?",
        options = listOf(
            "Skeletal system",
            "Muscular system",
            "Integumentary system",
            "Endocrine system"
        ),
        correctAnswer = 2,
        explanation = "The integumentary system (skin) regulates temperature and provides the first barrier against pathogens."
    )
)

fun getSimpleMachineQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion(
        question = "What is the mechanical advantage of a lever?",
        options = listOf(
            "The ratio of output force to input force",
            "The speed at which it moves",
            "The weight it can support",
            "The distance it can move"
        ),
        correctAnswer = 0,
        explanation = "Mechanical advantage is the ratio of output force to input force, showing how much a machine multiplies force."
    ),
    QuizQuestion(
        question = "Which simple machine is an inclined plane wrapped around a cylinder?",
        options = listOf(
            "Lever",
            "Pulley",
            "Screw",
            "Wedge"
        ),
        correctAnswer = 2,
        explanation = "A screw is essentially an inclined plane wrapped around a cylindrical core."
    ),
    QuizQuestion(
        question = "In a first-class lever, where is the fulcrum located?",
        options = listOf(
            "At one end",
            "Between the effort and load",
            "At the load",
            "At the effort"
        ),
        correctAnswer = 1,
        explanation = "In a first-class lever, the fulcrum is positioned between the effort force and the load."
    ),
    QuizQuestion(
        question = "What does a pulley system primarily change?",
        options = listOf(
            "The amount of work needed",
            "The direction of applied force",
            "The weight of the object",
            "The speed of movement"
        ),
        correctAnswer = 1,
        explanation = "Pulleys primarily change the direction of applied force, and can also provide mechanical advantage."
    ),
    QuizQuestion(
        question = "A wedge works by:",
        options = listOf(
            "Rotating around an axis",
            "Converting rotational to linear motion",
            "Concentrating force over a small area",
            "Changing the direction of force"
        ),
        correctAnswer = 2,
        explanation = "A wedge concentrates applied force over a small area to split, cut, or separate materials."
    )
)

fun getSolarSystemQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion(
        question = "Which planet is known as the 'Red Planet'?",
        options = listOf(
            "Venus",
            "Mars",
            "Jupiter",
            "Mercury"
        ),
        correctAnswer = 1,
        explanation = "Mars appears red due to iron oxide (rust) on its surface, earning it the nickname 'Red Planet'."
    ),
    QuizQuestion(
        question = "What is the largest planet in our solar system?",
        options = listOf(
            "Saturn",
            "Neptune",
            "Jupiter",
            "Uranus"
        ),
        correctAnswer = 2,
        explanation = "Jupiter is the largest planet, with a mass greater than all other planets combined."
    ),
    QuizQuestion(
        question = "How long does it take Earth to complete one orbit around the Sun?",
        options = listOf(
            "24 hours",
            "30 days",
            "365.25 days",
            "12 months exactly"
        ),
        correctAnswer = 2,
        explanation = "Earth takes approximately 365.25 days to orbit the Sun, which is why we have leap years."
    ),
    QuizQuestion(
        question = "Which planet has the most prominent ring system?",
        options = listOf(
            "Jupiter",
            "Saturn",
            "Uranus",
            "Neptune"
        ),
        correctAnswer = 1,
        explanation = "Saturn has the most extensive and visible ring system, made primarily of ice particles and rocky debris."
    ),
    QuizQuestion(
        question = "What causes the phases of the Moon?",
        options = listOf(
            "Earth's shadow on the Moon",
            "The Moon's rotation",
            "The Moon's changing position relative to Earth and Sun",
            "Clouds covering the Moon"
        ),
        correctAnswer = 2,
        explanation = "Moon phases are caused by the changing angles at which we see the sunlit portion of the Moon as it orbits Earth."
    )
)
