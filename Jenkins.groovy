// Параметры и глобальные переменные
task_branch = "${TEST_BRANCH_NAME}" // Получаем название ветки из параметра
def branch_cutted = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$branch_cutted"
base_git_url = "https://github.com/test80git/webfluxsecurity.git"

node {
    withEnv(["branch=${branch_cutted}", "base_url=${base_git_url}"]) {
        stage("Checkout Branch") {
            // Очистка workspace перед сборкой
            cleanWs()

            // Проверяем и получаем нужную ветку
            checkout scm: [
                    $class           : 'GitSCM',
                    branches         : [[name: "*/${branch_cutted}"]],
                    userRemoteConfigs: [[url: base_git_url]]
            ]
        }

        // Выполнение тестов параллельно
        try {
            parallel getTestStages(["apiTests", "uiTests"])
        } finally {
            stage ("Allure") {
                generateAllure()
            }
        }
        stage("Debug") {
            sh '''
        echo "Текущая директория:"
        pwd
        echo "Содержимое build/:"
        ls -la build/ || echo "Нет build/"
        echo "Поиск allure-results:"
        find . -name "allure-results" -type d 2>/dev/null
    '''
        }
    }
}

// Вспомогательные методы

def getTestStages(testTags) {
    def stages = [:]
    testTags.each { tag ->
        stages["${tag}"] = {
            runTestWithTag(tag)
        }
    }
    return stages
}

def runTestWithTag(String tag) {
    try {
        labelledShell(label: "Run ${tag}", script: "chmod +x gradlew \n./gradlew -x test ${tag}")
    } finally {
        echo "Some failed tests detected"
    }
}

def generateAllure() {
    allure([
            includeProperties: true,
            jdk              : '',
            properties       : [],
            reportBuildPolicy: 'ALWAYS',
            results          : [[path: 'build/reports/allure-results']]
    ])
}