package edu.pjatk.inn.coffeemaker;

import edu.pjatk.inn.coffeemaker.impl.CoffeeMaker;
import edu.pjatk.inn.coffeemaker.impl.Inventory;
import edu.pjatk.inn.coffeemaker.impl.Recipe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.service.ContextException;
import sorcer.service.Routine;

import static org.junit.Assert.*;
import static sorcer.eo.operator.*;
import static sorcer.so.operator.exec;

/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/coffeemaker")
public class CoffeeMakerTest {
	private final static Logger logger = LoggerFactory.getLogger(CoffeeMakerTest.class);

	private CoffeeMaker coffeeMaker;
	private Inventory inventory;
	private Recipe espresso, mocha, latte, americano;

	@Before
	public void setUp() throws ContextException {
		coffeeMaker = new CoffeeMaker();
		inventory = coffeeMaker.checkInventory();

		espresso = new Recipe();
		espresso.setName("espresso");
		espresso.setPrice(50);
		espresso.setAmtCoffee(6);
		espresso.setAmtMilk(1);
		espresso.setAmtSugar(1);
		espresso.setAmtChocolate(0);

		mocha = new Recipe();
		mocha.setName("mocha");
		mocha.setPrice(100);
		mocha.setAmtCoffee(8);
		mocha.setAmtMilk(1);
		mocha.setAmtSugar(1);
		mocha.setAmtChocolate(2);

		latte = new Recipe();
		latte.setName("latte");
		latte.setPrice(40);
		latte.setAmtCoffee(2);
		latte.setAmtMilk(5);
		latte.setAmtSugar(2);
		latte.setAmtChocolate(0);

		americano = new Recipe();
		americano.setName("americano");
		americano.setPrice(40);
		americano.setAmtCoffee(7);
		americano.setAmtMilk(1);
		americano.setAmtSugar(2);
		americano.setAmtChocolate(0);
	}

	@Test
	public void testAddRecipe() {
		assertTrue(coffeeMaker.addRecipe(espresso));
	}

	@Test
	public void testContextCofee() throws ContextException {
		assertTrue(espresso.getAmtCoffee() == 6);
	}

	@Test
	public void testContextMilk() throws ContextException {
		assertTrue(espresso.getAmtMilk() == 1);
	}

	@Test
	public void addRecepie() throws Exception {
		coffeeMaker.addRecipe(mocha);
		assertEquals(coffeeMaker.getRecipeForName("mocha").getName(), "mocha");
	}

	@Test
	public void addContextRecepie() throws Exception {
		coffeeMaker.addRecipe(Recipe.getContext(mocha));
		assertEquals(coffeeMaker.getRecipeForName("mocha").getName(), "mocha");
	}

	@Test
	public void addServiceRecepie() throws Exception {
		Routine cmt = task(sig("addRecipe", coffeeMaker),
				context(types(Recipe.class), args(espresso),
						result("recipe/added")));

		logger.info("isAdded: " + exec(cmt));
		assertEquals(coffeeMaker.getRecipeForName("espresso").getName(), "espresso");
	}

	@Test
	public void addRecipes() throws Exception {
		coffeeMaker.addRecipe(mocha);
		coffeeMaker.addRecipe(latte);
		coffeeMaker.addRecipe(americano);

		assertEquals(coffeeMaker.getRecipeForName("mocha").getName(), "mocha");
		assertEquals(coffeeMaker.getRecipeForName("latte").getName(), "latte");
		assertEquals(coffeeMaker.getRecipeForName("americano").getName(), "americano");
	}

	@Test
	public void makeCoffee() throws Exception {
		coffeeMaker.addRecipe(espresso);
		assertEquals(coffeeMaker.makeCoffee(espresso, 200), 150);
	}

	@Test
	public void addRecipe_MoreThan3Added(){
		assertTrue(coffeeMaker.addRecipe(espresso));
		assertTrue(coffeeMaker.addRecipe(mocha));
		assertTrue(coffeeMaker.addRecipe(latte));
		assertFalse(coffeeMaker.addRecipe(americano));
	}

	@Test
	public void addRecipe_FirstSpotChosen(){
		coffeeMaker.addRecipe(espresso);
		assertEquals(espresso, coffeeMaker.getRecipes()[0]);
		assertTrue(coffeeMaker.getRecipeFull()[0]);
	}

	@Test
	public void deleteRecipeTest_Deleted(){
		coffeeMaker.addRecipe(espresso);
		coffeeMaker.deleteRecipe(espresso);
		assertEquals("", coffeeMaker.getRecipes()[0].getName());
		assertFalse(coffeeMaker.getRecipeFull()[0]);
	}

	@Test
	public void editRecipeTest_recipeEdited(){
		coffeeMaker.addRecipe(espresso);
		coffeeMaker.editRecipe(espresso,mocha);
		assertEquals(mocha, coffeeMaker.getRecipes()[0]);
	}
	@Test
	public void makeCoffeeNotEnoughMoney(){
		int amtPaid = 49;
		assertEquals(amtPaid, coffeeMaker.makeCoffee(espresso, amtPaid));
	}

	@Test
	public void makeCoffeeNotEnoughIngredients(){
		int amtCoffee = 1;
		int amtMilk = 1;
		int amtSugar = 1;
		int amtChocolate = 1;
		inventory.setCoffee(amtCoffee);
		inventory.setCoffee(amtMilk);
		inventory.setCoffee(amtSugar);
		inventory.setCoffee(amtChocolate);

		int amtPaid = 50;
		assertEquals(amtPaid, coffeeMaker.makeCoffee(espresso, amtPaid));
	}

	@Test
	public void makeCoffeeInventoryCheck(){
		int amtPaid = 50;
		coffeeMaker.makeCoffee(espresso, amtPaid);
		assertEquals(9, inventory.getCoffee());
		assertEquals(14, inventory.getMilk());
		assertEquals(14, inventory.getSugar());
		assertEquals(15, inventory.getChocolate());
	}


	@Test
	public void addInventory(){
		int amtCoffee = 6;
		int amtMilk = 4;
		int amtSugar = 4;
		int amtChocolate = 3;

		int coffeeBefore = inventory.getCoffee();
		int milkBefore = inventory.getMilk();
		int sugarBefore = inventory.getSugar();
		int chocolateBefore = inventory.getChocolate();

		assertTrue(coffeeMaker.addInventory(amtCoffee, amtMilk, amtSugar, amtChocolate));

		assertEquals(coffeeBefore + amtCoffee, inventory.getCoffee());
		assertEquals(milkBefore + amtMilk, inventory.getMilk());
		assertEquals(sugarBefore + amtSugar, inventory.getSugar());
		assertEquals(chocolateBefore + amtChocolate, inventory.getChocolate());
	}

}

