### To get started using Gripes 
Checkout: https://github.com/gripes/gripes-example

### To use Gripes within a current Gradle build:
Add the following to `build.gradle`

	buildscript { 
		versions = [
			'gripes-web' : '0.1.8',
			'gripes-plugin' : '0.1.9',
			'gripersist' : '0.1.5',
			'hibernate-core' : '4.0.0.Beta2'
		]
		repositories {
			mavenCentral()
			mavenRepo urls: "http://www.gripes-project.org/libs"
		}		
		dependencies {
			classpath group: 'gripes', name: 'gripes-plugin', version: versions['gripes-plugin']
			classpath group: 'gripes', name: 'gripes-web', version: versions['gripes-web']
			classpath group: 'org.hibernate', name: 'hibernate-core', version: versions['hibernate-core']
			classpath group: 'org.hibernate', name: 'hibernate-entitymanager', version: versions['hibernate-core']
		}
	}
	
Then

1. Run: 'gradle init'
2. Gripes will update your build.gradle file, edit the gripes{} section accordingly
3. Run: 'gradle setup'
4. First entity class: 'gradle create -Pmodel=Page'
5. Edit the entity, adding properties (i.e. String name, description)
6. Create actions: 'gradle create -Paction=Page'
7. Run the app: 'gradle run'
8. Browse: http://localhost:8888/gripes