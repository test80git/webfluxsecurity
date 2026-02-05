// Параметры
task_branch = "${TEST_BRANCH_NAME}"
def branch_cutted = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$branch_cutted"
base_git_url = "https://github.com/test80git/webfluxsecurity.git"

node {
    withEnv(["branch=${branch_cutted}", "base_url=${base_git_url}"]) {

        stage("Checkout Branch") {
            cleanWs()
            checkout scm: [
                    $class           : 'GitSCM',
                    branches         : [[name: "*/${branch_cutted}"]],
                    userRemoteConfigs: [[url: base_git_url]]
            ]
        }

        try {
            parallel getTestStages(["apiTests", "uiTests"])
        } finally {
            stage("Reports BlueOcean"){
                // Собираем JUnit отчеты для Blue Ocean
                junit 'build/test-results/**/*.xml'
            }
            stage("Allure Report") {
                generateAllure()
            }
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
        sh """
            chmod +x gradlew
            ./gradlew clean ${tag}
        """
    } catch (err) {
        echo "Тесты завершились с ошибками: ${err}"
        currentBuild.result = 'UNSTABLE'
    }
}

def generateAllure() {
    allure([
            includeProperties: true,
            jdk              : '',
            properties       : [],
            reportBuildPolicy: 'ALWAYS',
            results          : [[path: 'build/allure-results']]
    ])
}