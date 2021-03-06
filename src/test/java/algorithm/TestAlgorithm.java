package algorithm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import crowdtrust.AnnotationType;
import crowdtrust.BinaryAccuracy;
import crowdtrust.BinaryResponse;
import crowdtrust.BinarySubTask;
import crowdtrust.Account;
import crowdtrust.InputType;
import crowdtrust.MediaType;
import crowdtrust.Response;

import db.DbInitialiser;
import db.LoginDb;
import db.RegisterDb;
import db.SubTaskDb;
import db.TaskDb;
import db.CrowdDb;

import junit.framework.TestCase;
/*
 * TEAM PLEASE NOTE: this has now been superceeded by TestAlgorithmComplete which
 * encompasses all types of tasks.
 */
public class TestAlgorithm extends TestCase {

	//change to test scaleability
	protected static int annotatorNumber = 50;
	protected static int subtasks = 200;
	
	protected static int totalPos = 1000;	//Annotators when created have 
	protected static int totalNeg = 1000;   //'Answered' 2000 questions
	
	protected AnnotatorModel[] annotators;
	
	public TestAlgorithm(String name){
		super(name);
	}
	
	public void testAlgorithm(){
		boolean labs = false;
		System.setProperty("test", "true");
		if(labs){
			DbInitialiser.init();
		}
		//Lets create some annotators with id's 1 - 1000 and place them in array
		annotators = new AnnotatorModel[annotatorNumber];
		for(int i = 0; i < annotatorNumber; i++){
			String uuid = UUID.randomUUID().toString();
			uuid = uuid.replace("-", "");
			uuid = uuid.substring(0, 12);
			annotators[i] = new AnnotatorModel(uuid, uuid);
		}
		
		//Set up the annotators so they can answer binary question
		Random rand = new Random();
		for(int i = 0; i < annotatorNumber; i++){
			//int truePos = rand.nextInt(999) + 1;	
			//int trueNeg = rand.nextInt(999) + 1;
			//annotators[i].setUpBinary(truePos, trueNeg, totalPos, totalNeg);
			double percentageNormal = 0.75;
			if(rand.nextDouble() > percentageNormal){
				annotators[i].setUpBinary(500, 500, totalPos, totalNeg);
			}else{
				annotators[i].setUpBinary(850, 850, 1000, 1000);
			}
			
		}
		
		if(labs){
		//Add them to the Database
		for(int i = 0; i < annotatorNumber; i++){
			RegisterDb.addUser("test@test.com", annotators[i].getUsername(), annotators[i].getPassword(), true);
			annotators[i].setId(LoginDb.checkUserDetails(annotators[i].getUsername(), annotators[i].getPassword()));
			AnnotatorModel a = annotators[i];
			System.out.println("annotator " + 
					a.bee.getId() + 
					" truePosRate =" + a.binary.truePosRate +
					" trueNegRate =" + a.binary.trueNegRate);
		}
		
		//Lets make a client
		RegisterDb.addUser("testClient@test.com", "gio", "gio", false);
		int accountId = LoginDb.checkUserDetails("gio", "gio");
		//Lets add a binary task to the database
		long expiry = getDate();
		float accuracy = (float)0.7;
		List<String> testQs = new LinkedList<String>();
		testQs.add("test q1");
		testQs.add("test q2");
		assertTrue(TaskDb.addTask(accountId,"BinaryTestTask", "This is a test?", accuracy, MediaType.IMAGE, AnnotationType.BINARY, InputType.RADIO, 15, expiry, testQs, new LinkedList<String>(), new LinkedList<String>(), 0)>0);
		
		//List of answers
		LinkedList<AnnotatorSubTaskAnswer> answers = new LinkedList<AnnotatorSubTaskAnswer>();
		System.out.println("About to get Task id");
		System.out.println("John Task Id: " + TaskDb.getTaskId("BinaryTestTask"));
		System.out.println("Got it");
		
		//Lets create a linked list of subTasks
		for(int i = 0; i < subtasks; i++){
			String uuid = UUID.randomUUID().toString();
			uuid = uuid.replace("-", "");
			uuid = uuid.substring(0, 12);
			SubTaskDb.addSubtask(uuid, TaskDb.getTaskId("BinaryTestTask"));
			int id = SubTaskDb.getSubTaskId(uuid);
			System.out.println("Subtask Id: " + id);
			BinarySubTask bst = new BinarySubTask(id,0.7,0,15);
			AnnotatorSubTaskAnswer asta = new AnnotatorSubTaskAnswer(bst.getId(), bst, new BinaryTestData(rand.nextInt(2)));
			answers.add(asta);
		}
		
		//Give all the annotators the answers
		for(int i = 0; i < annotatorNumber; i++){
			annotators[i].setTasks(answers);
		}
		System.out.println("Given annotators answers");
		
		printAnswers(answers);
		System.out.println("---------Beginning to answer tasks--------------------");
		
		int parent_task_id = TaskDb.getTaskId("BinaryTestTask");
		
		int annotatorIndex = rand.nextInt(annotatorNumber - 1);
		AnnotatorModel a = annotators[annotatorIndex];
		System.out.println("Getting first random");
		BinarySubTask t = (BinarySubTask) SubTaskDb.getRandomSubTask(parent_task_id, a.bee.getId());
		System.out.println("Got it");
		
		while( t != null){
			annotatorIndex = rand.nextInt(annotatorNumber - 1);
			a = annotators[annotatorIndex];
			System.out.println("Annotator: " + a.username + " |Task: " + t.getId());
			a.answerTask(t);
			t = (BinarySubTask) SubTaskDb.getRandomSubTask(parent_task_id, a.bee.getId());
		} 
		System.out.println("------------------------------------------------------  ");
		
		System.out.println("---------Calculating label error rate--------------------");
		
		Map<Integer,Response> results = SubTaskDb.getMappedResults(1);
		int correct = 0;
		for (AnnotatorSubTaskAnswer answer : answers){
			Response trueA = answer.getAlgoTestData().getActualAnswer();
			Response estA = results.get(answer.id);
			System.out.println("id " + answer.id + 
					" true answer = " + 
					trueA + 
					" estimate = " + estA);
			if(trueA.equals(estA)){
				correct++;
			}
		}
		System.out.println("success rate = " + ((double)correct/subtasks));

		System.out.println("------------------------------------------------------  ");
		
		System.out.println("----------------Offline Binary Testing-------------------");
		System.out.println("Id |    ATPR    |    ATNR    |    TPRE    |    TNRE    ");
			for(int i = 0; i < annotatorNumber; i++){
				int totalQuestions = 1000;
				int negQuestions   = 0;
				int posQuestions   = 0;
				int truePos        = 0;
				int trueNeg        = 0;
				AnnotatorModel annotator = annotators[i];
				System.out.print(annotator.getBee().getId() +" | " + annotator.getBinaryBehaviour().getTruePosRate() + " | " + annotator.getBinaryBehaviour().getTrueNegRate() + " | " );
				for(int j = 0; j < totalQuestions; j++){
					Random r = new Random();
					int actualAnswer   = r.nextInt(2);
					boolean answerBool;
					if(actualAnswer == 1){
						answerBool = true;
						posQuestions++;
					}else{
						answerBool = false;
						negQuestions++;
					}
					int annotatorAnswer = annotators[i].getBinaryBehaviour().generateAnswer(new BinaryResponse(answerBool));
					if(annotatorAnswer == 1 & actualAnswer == 1){
						truePos ++;
					}else if(annotatorAnswer == 0 & actualAnswer == 0){
						trueNeg ++;
					}
				}
				double tpr = ((truePos * 1.0) / (posQuestions * 1.0));
				double tnr = ((trueNeg * 1.0) / (negQuestions * 1.0));
						
				System.out.print(tpr + " | " + tnr);
				System.out.println("");
				System.out.println("NumPos = " + posQuestions + " NumNeg = " + negQuestions + " TruePos = " + truePos  + " TrueNeg = " + trueNeg);
				System.out.println("");
			}
		System.out.println("----------------------------------------------------------");
		
		System.out.println("----------Calculating Annotator Rates-----------------");
		System.out.println("Id |    TPR    |    TNR    |    TPRE    |    TNRE    ");
			for(int i = 0; i < annotatorNumber; i++){
				AnnotatorModel annotator = annotators[i];
				System.out.print(annotator.getBee().getId() +" | " + annotator.getBinaryBehaviour().getTruePosRate() + " | " + annotator.getBinaryBehaviour().getTrueNegRate() + " | " );
				BinaryAccuracy binAccuracy = CrowdDb.getBinaryAccuracy(annotator.getBee().getId());
				System.out.print(binAccuracy.getTruePositive() +" | "+ binAccuracy.getTrueNegative());
				System.out.println("");
			}
		System.out.println("------------------------------------------------------");
//

		
	/*	System.out.println("---------Calculating accuracy average difference--------------------");
		
		Map<Integer,Response> accuracies = SubTaskDb.getResults(1);
		for (AnnotatorSubTaskAnswer answer : answers){
			Response trueA = answer.getAlgoTestData().getActualAnswer();
			Response estA = results.get(answer.id);
			System.out.println("id " + answer.id + 
					" true answer = " + 
					trueA + 
					" estimate = " + estA);
			if(trueA.equals(estA)){
				correct++;
			}
		}
		System.out.println("error rate = " + (correct/subtasks));
		
		System.out.println("------------------------------------------------------ "); */

		
		//DbInitialiser.init();
		}
		
		
	}
	
	protected void printAnswers(LinkedList<AnnotatorSubTaskAnswer> answers){
		System.out.println("-------------Printing Answers------------------");
		Iterator<AnnotatorSubTaskAnswer> i = answers.iterator();
		while(i.hasNext()){
			AnnotatorSubTaskAnswer temp = i.next();
			System.out.println("Answer id: " + temp.getId());
			System.out.println("Actual answer: " + ((BinaryTestData)temp.getAlgoTestData()).getActualAnswer());
		}
		System.out.println("-----------------------------------------------");
	}
	
	protected long getDate(){
		long ret        = 0           ;
		String str_date = "11-June-15";
	    DateFormat formatter ; 
	    Date date ; 
	    formatter = new SimpleDateFormat("dd-MMM-yy");
		try {
			date = (Date)formatter.parse(str_date);
			ret = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		return ret;
	}
	
	protected void printExpertList(){
		System.out.println("-----------Printing Expert List----------------");
		System.out.println("-----------------------------------------------");
		List<Account> experts = CrowdDb.getAllExperts();
		for(Account account : experts) {
			System.out.println("id =" + account.getId() + " name = " + account.getName());
		}
	}
	
	protected void printBotList(){
		System.out.println("-----------Printing Bots List-------------------");
		System.out.println("------------------------------------------------");
		List<Account> bots = CrowdDb.getAllExperts();
		for(Account account : bots) {
			System.out.println("id =" + account.getId() + " name = " + account.getName());
		}
	}
}
