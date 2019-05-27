package io.kweb.demos.todo

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.SeleniumExtension
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Test for the todoApp demo
 */
@ExtendWith(SeleniumExtension::class)
class TodoDemoTest {
    companion object {
        lateinit var todoKweb:TodoApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            todoKweb = TodoApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            todoKweb.server.close()
        }
    }

    @Test
    fun pageRenders(@Arguments("--headless") driver:ChromeDriver){
        val site = TodoSite(driver)
        site.allElementsExist().shouldBeTrue()
    }

    @Test
    fun enterNewItem(@Arguments("--headless") driver:ChromeDriver){
        val todoItem = "feel like an ocean, warmed by the sun"
        val site = TodoSite(driver)
        site.addTodoItem(todoItem)
        val itemElem = WebDriverWait(driver, 5).until {
            driver.findElement(
                    By.xpath("//div[contains(text(),'$todoItem') and @class='content']"))
        }
        itemElem?.isDisplayed?.shouldBeTrue()
    }

    @Test
    fun multipleUsers(@Arguments("--headless") driver1:ChromeDriver,
                      @Arguments("--headless") driver2:ChromeDriver){
        val todoItem = "bring me a great big flood"
        val site = TodoSite(driver1)

        //make sure we go to the same list the first driver was redirected to
        driver2.get(driver1.currentUrl)

        //after both pages have loaded, add item via first driver
        site.addTodoItem(todoItem)

        //make sure it appears for second driver
        val itemElem = WebDriverWait(driver2, 5).until {
            driver2.findElement(
                    By.xpath("//div[contains(text(),'$todoItem') and @class='content']"))
        }
        itemElem?.isDisplayed?.shouldBeTrue()
    }

    @Test
    fun deleteItems(@Arguments("--headless") driver:ChromeDriver){
        val firstItem = "We'll be all right"
        val secondItem = "Stay here some time"
        val thirdItem = "This country dog won't die in the city"

        val site = TodoSite(driver)
        site.addTodoItem(firstItem)
        site.addTodoItem(secondItem)
        site.addTodoItem(thirdItem)

        site.deleteItemByText(secondItem)

        val allItems = site.getAllItems()
        allItems.find{it.text == firstItem}.shouldNotBeNull()
        allItems.find{it.text == secondItem}.shouldBeNull()
        allItems.find{it.text == thirdItem}.shouldNotBeNull()
    }
}

class TodoSite(private val driver: WebDriver){

    @FindBy(className = "message")
    val message: WebElement? = null

    @FindBy(tagName = "h1")
    val header: WebElement? = null

    @FindBy(tagName = "input")
    val input: WebElement? = null

    @FindBy(xpath = "//button[text()='Add']")
    val addButton: WebElement? = null

    fun allElementsExist() : Boolean {
        return message?.isDisplayed ?: false
                && header?.isDisplayed ?: false
                && input?.isDisplayed ?: false
                && addButton?.isDisplayed ?: false
    }

    fun addTodoItem(item:String){
        input?.sendKeys(item)
        addButton?.click()
    }

    fun getAllItems() : List<WebElement> {
        return driver.findElements<WebElement>(By.xpath("//div[@class='item']"))
    }

    fun deleteItemByText(itemText:String){
        val allItems = getAllItems()
        val item = allItems.find{ it.text == itemText }
        val delButton = item?.findElement<WebElement>(By.tagName("button"))
        delButton?.click()
    }

    init {
        if(!driver.currentUrl.startsWith("http://localhost:7659/lists")){
            //if not on correct page, navigate there when page object inits
            driver.get("http://localhost:7659")
        }
        PageFactory.initElements(driver, this)
    }
}