import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

abstract class StateKS
{


}


class Review 
{
    String username;
    String product;
    String message;
    String picture;

    boolean drop = false;

    List<StateKS> StatesList = new ArrayList<StateKS>();

    Review(String username, String product, String message, String picture)
    {
        this.username = username;
        this.product = product;
        this.message = message;
        this.picture = picture;
    }

    @Override
    public String toString() 
    {
        return username + "," + product + "," + message + "," + picture;
    } 
}

class ProductCustomerDB
{
    ArrayList<ArrayList<String>> ProdCusLists = new ArrayList<ArrayList<String>>();

    ArrayList<String> GetListOfCustomers(int product)
    {
        return ProdCusLists.get(product);
    }

    void AddList(int product, ArrayList<String> List)
    {
        ProdCusLists.add(product, List);
    }

}

abstract class KnowledgeSource implements Runnable
{
    Review review = null;

    ArrayBlockingQueue<Review> blackBoard;

    List<StateKS> dropperStates;

    StateKS ownState;


    KnowledgeSource(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates)
    {
        this.blackBoard = blackBoard;
        this.dropperStates = dropperStates;
    }


    abstract void execute();


    void execCondition()
    {
        //this.review = blackBoard.poll();

        try 
        {
            this.review = blackBoard.take();

        } catch (Exception e) 
        {
            e.printStackTrace();
        }

       
        //for (Review review : blackBoard) 
        {
            if(review.StatesList.contains(ownState))
            {
                PutReview();       
            }
            else
            {
                if(dropperStates.contains(ownState))
                {
                    execute();
                    // get to the first review that matches the condition;
                    //break;
                }
                else
                {
                    boolean checkDropperStates = true;

                    for(StateKS dropperState : dropperStates)
                    {
                        if(!review.StatesList.contains(dropperState)) 
                        {
                            checkDropperStates = false;
                            break;
                        }
                    }

                    if(checkDropperStates)
                    {
                        execute();
                         // get to the first review that matches the condition;
                        //break;
                    }
                    else PutReview();
  
                }
            }   
        }
        //if(review != null) System.out.println(review.toString());
    }


    void PutReview()
    {
        Random rnd = new Random();
        if(review.drop) 
        {
            this.review = null;
            System.out.println("Killed review");
            try 
            {
                Thread.sleep(rnd.nextInt(800)+200);
       
            } catch (Exception e) 
            {
                e.printStackTrace();
            }

            return;
        }

        try
        {
            //System.out.println("passed review");
            blackBoard.put(review);
            this.review = null;
            try 
            {
                Thread.sleep(rnd.nextInt(800)+200);
       
            } catch (Exception e) 
            {
                e.printStackTrace();
            }
            
            

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }  
}

class OutputState extends StateKS
{

}

class OutputKS extends KnowledgeSource
{
    int statesCount;

   OutputKS(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates, int count)
   {
       super(blackBoard, dropperStates);
       this.ownState = new OutputState();
       this.statesCount = count;
   }


    @Override
    void execute() 
    {

        if(review.StatesList.size() == statesCount)
        {
            System.out.println(review.toString());

            review = null;
        }
        else PutReview();    
    }
   
    @Override
    public void run() 
    {
        while(true)
        {
            while(review == null) this.execCondition();
             
        }
        
    }
}

class ProfanityState extends StateKS
{

}

class ProfanityKS extends KnowledgeSource
{
    List<String> profanityList;

    public ProfanityKS(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates, List<String> profList)
    {
        super(blackBoard, dropperStates);
        this.ownState = new ProfanityState();
        this.profanityList = profList;
    }

    @Override
    void execute() 
    {
        boolean foundProfanity = false;
        String message = review.message;
        //System.out.println(message);

        for (String profanity : profanityList) 
        {
            if(message.contains(profanity))
            {
                foundProfanity = true;
                //System.out.println("CONTAINS PROFANITY");

                review.drop = true;
                break;
            }
        }

        review.StatesList.add(ownState);

        PutReview();
    }

    @Override
    public void run() 
    {    
        while(true)
        {
            while(review == null) this.execCondition();
             
        }
    } 
    
}

class PropagandaState extends StateKS
{

}

class PropagandaKS extends KnowledgeSource
{
    List<String> propList;

    public PropagandaKS(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates, List<String> propList)
    {
        super(blackBoard, dropperStates);
        this.ownState = new PropagandaState();
        this.propList = propList;
    }

    @Override
    void execute() 
    {
        String message = review.message;

            for (String propaganda : propList)
            {
                if(message.contains(propaganda))
                {
                    //System.out.println("contains " + propaganda);
                    String regex = "[" + propaganda + "]";
                    message = message.replaceAll(regex, "");
                }
            }

        review.message = message;

        review.StatesList.add(ownState);

        PutReview();
    }

    @Override
    public void run() 
    {
        while(true)
        {
            while(review == null) this.execCondition();
             
        }
    }
    
}

class BuyerCheckState extends StateKS
{

}



class BuyerCheckKS extends KnowledgeSource
{
    Hashtable<String, List<String>> ProductBuyerTable;

    public BuyerCheckKS(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates, Hashtable<String, List<String>> ProductBuyerTable)
    {
        super(blackBoard, dropperStates);
        this.ownState = new BuyerCheckState();
        this.ProductBuyerTable = ProductBuyerTable;
    }

    @Override
    void execute() 
    {
        String product = review.product;
        String user = review.username;

        if(!ProductBuyerTable.containsKey(product))
        {
            //System.out.println("no such product");
            review.drop = true;
        }
        else
        {
            List<String> ProductBuyers = ProductBuyerTable.get(product);
            if(!ProductBuyers.contains(user)) review.drop = true;
            //else System.out.println("Works");
        }


        review.StatesList.add(ownState);
        PutReview();
    }

    @Override
    public void run() 
    {
        while(true)
        {
            while(review == null) this.execCondition();
             
        }    
    }
}


class PictureState extends StateKS
{

}


class PictureKS extends KnowledgeSource
{
    PictureKS(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates)
    {
        super(blackBoard, dropperStates);
        this.ownState = new PictureState();  
    }

    @Override
    void execute() 
    {
        String picture = review.picture;
        picture = picture.toLowerCase();

        review.picture = picture;


        review.StatesList.add(ownState);
        PutReview();
    }

    @Override
    public void run() 
    {
        while(true)
        {
            while(review == null) this.execCondition();
             
        }
    }   
}

class SentimentState extends StateKS
{

}


class SentimentKS extends KnowledgeSource
{
    SentimentKS(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates)
    {
        super(blackBoard, dropperStates);
        this.ownState = new SentimentState();  
    }

    @Override
    void execute() 
    {
        String message = review.message;
        int len = message.length();

        int sum = 0;

        for(int i = 0; i < len; i++)
        {
            if(Character.isUpperCase((message.charAt(i)))) sum++;
            else                                           sum--;
        }

        if(sum == 0)     review.message = message.concat("=");
        else if(sum > 0) review.message = message.concat("+");
        else             review.message = message.concat("-");

            
        review.StatesList.add(ownState);
        PutReview();
        
    }

    @Override
    public void run() 
    {
        while(true)
        {
            while(review == null) this.execCondition();    
        }
    }   
}


class RemoveCompState extends StateKS
{

}


class RemoveCompKS extends KnowledgeSource
{
    RemoveCompKS(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates)
    {
        super(blackBoard, dropperStates);
        this.ownState = new RemoveCompState();  
    }

    @Override
    void execute() 
    {
        String message =review.message;
        if(message.contains("http"))
        {
            //System.out.println("CONTAINS HTTP");
            message = message.replaceAll("[http]", "");
        }

        review.message = message;

        review.StatesList.add(ownState);

        PutReview();
        
    }

    @Override
    public void run() 
    {
        while(true)
        {
            while(review == null) this.execCondition();    
        }    
    }
}


public class BlackBoard
{
    public static void main(String[] args) 
    {
        System.out.println("Guten Tag");

        Hashtable<String, List<String>> ProductBuyerTable = new Hashtable<>();

        List<String> LaptopBuyers = new ArrayList<String>();
        LaptopBuyers.add("John");
        LaptopBuyers.add("Alice");
        LaptopBuyers.add("Catherine");
        LaptopBuyers.add("Olivia");
        LaptopBuyers.add("Maya");
        LaptopBuyers.add("Yara");
        LaptopBuyers.add("Uma");
        LaptopBuyers.add("Olga");

        ProductBuyerTable.put("Laptop", LaptopBuyers);

        List<String> PhoneBuyers = new ArrayList<String>();
        PhoneBuyers.add("Mary");
        PhoneBuyers.add("Jack");
        PhoneBuyers.add("Frank");
        PhoneBuyers.add("Liam");
        PhoneBuyers.add("Rita");

        ProductBuyerTable.put("Phone", PhoneBuyers);

        List<String> porfanityList = new ArrayList<String>(10);
        porfanityList.add("@#$%");

        List<String> PropagandaList = new ArrayList<String>();
        PropagandaList.add("+++");
        PropagandaList.add("---");

    Review m1 = new Review("John", "Laptop", "http---o+++k +++", "PICTURE");
    Review m2 = new Review("Mary", "Phone", "httpabcd  @#$%", "IMAGE");
    Review m3 = new Review("Alice", "Laptop", "https://laptop-reviews.com ++", "PHOTO");
    Review m4 = new Review("Bob", "Phone", "http://phone-review@@##", "IMAGE");
    Review m5 = new Review("Catherine", "Laptop", "http--##laptop111", "PHOTO");
    Review m6 = new Review("David", "Phone", "https://phone-shop++", "PICTURE");
    Review m7 = new Review("Eve", "Laptop", "http+++hello+laptop", "IMAGE");
    Review m8 = new Review("Frank", "Phone", "http://phone-review***", "PHOTO");
    Review m9 = new Review("Grace", "Laptop", "http---review-for-laptop", "IMAGE");
    Review m10 = new Review("Henry", "Phone", "https://phone-buy+++now", "PHOTO");
    Review m11 = new Review("Isla", "Laptop", "http://laptop123+++", "IMAGE");
    Review m12 = new Review("Jack", "Phone", "https://shop-phone--@#$%", "PHOTO");
    Review m13 = new Review("Kylie", "Laptop", "http://laptop-experience---", "IMAGE");
    Review m14 = new Review("Liam", "Phone", "http+++mobile-review", "PICTURE");
    Review m15 = new Review("Maya", "Laptop", "https://laptop-shop@@", "PHOTO");
    Review m16 = new Review("Noah", "Phone", "http---phone-warranty+++", "IMAGE");
    Review m17 = new Review("Olivia", "Laptop", "http://laptop12345", "PHOTO");
    Review m18 = new Review("Paul", "Phone", "https://mobile-review-%%", "PICTURE");
    Review m19 = new Review("Quinn", "Laptop", "http+phone-buy-now", "PHOTO");
    Review m20 = new Review("Rita", "Phone", "http://phone-link+++xxx", "IMAGE");
    Review m21 = new Review("Sam", "Laptop", "https://laptop-ratings--", "PHOTO");
    Review m22 = new Review("Tina", "Phone", "http://phone-special-offer", "IMAGE");
    Review m23 = new Review("Uma", "Laptop", "http://laptop-review-123", "PHOTO");
    Review m24 = new Review("Victor", "Phone", "http+++phone-purchase-now", "PICTURE");
    Review m25 = new Review("Wendy", "Laptop", "http://best-laptop-reviews", "PHOTO");
    Review m26 = new Review("Xander", "Phone", "https://new-phone-model+@", "IMAGE");
    Review m27 = new Review("Yara", "Laptop", "http+++laptop-reviews-now", "PHOTO");
    Review m28 = new Review("Zane", "Phone", "http://phone-special-deal", "IMAGE");
    Review m29 = new Review("Aaron", "Laptop", "https://laptop-article@#$", "PHOTO");
    Review m30 = new Review("Briana", "Phone", "http--new-phone-review", "IMAGE");
    Review m31 = new Review("Chloe", "Laptop", "https://laptop-test-review", "PHOTO");
    Review m32 = new Review("Derek", "Phone", "http://mobile-experience", "PICTURE");
    Review m33 = new Review("Eva", "Laptop", "http://laptop-site@@", "IMAGE");
    Review m34 = new Review("Fred", "Phone", "http://latest-phone-deal", "PHOTO");
    Review m35 = new Review("Gina", "Laptop", "http+++laptop-deal-special", "IMAGE");
    Review m36 = new Review("Holly", "Phone", "http://phone-test-link", "PICTURE");
    Review m37 = new Review("Ian", "Laptop", "http://review-laptop-123++", "PHOTO");
    Review m38 = new Review("Jackie", "Phone", "https://mobile-review-now", "IMAGE");
    Review m39 = new Review("Karl", "Laptop", "http://laptop-shoppers@##", "PICTURE");
    Review m40 = new Review("Lena", "Phone", "https://phone-deal-special", "PHOTO");
    Review m41 = new Review("Mike", "Laptop", "https://best-laptop-2025", "IMAGE");
    Review m42 = new Review("Nina", "Phone", "http--new-phone-model", "PHOTO");
    Review m43 = new Review("Olga", "Laptop", "http://new-laptop-adv", "IMAGE");
    Review m44 = new Review("Penny", "Phone", "https://phone-discount-%%", "PHOTO");
    Review m45 = new Review("Quincy", "Laptop", "http://laptop-5-stars---", "IMAGE");
    Review m46 = new Review("Rachel", "Phone", "http://best-phone-review-now", "PICTURE");
    Review m47 = new Review("Steve", "Laptop", "http://laptop-shop-online", "PHOTO");
    Review m48 = new Review("Tracy", "Phone", "https://phone-ratings@##", "IMAGE");
    Review m49 = new Review("Ursula", "Laptop", "http://laptop-purchase-now", "PHOTO");
    Review m50 = new Review("Vince", "Phone", "https://phone-check-now", "IMAGE");

        ArrayBlockingQueue<Review> blackBoard = new ArrayBlockingQueue<Review>(40);

        ArrayList<StateKS> dropperStates = new ArrayList<StateKS>(5);

        ProfanityKS profKS = new ProfanityKS(blackBoard, dropperStates, porfanityList);

        dropperStates.add(profKS.ownState);

        OutputKS outKS = new OutputKS(blackBoard, dropperStates, 6);

        PropagandaKS propKS = new PropagandaKS(blackBoard, dropperStates, PropagandaList);

        BuyerCheckKS buyerKS = new BuyerCheckKS(blackBoard, dropperStates, ProductBuyerTable);

        dropperStates.add(buyerKS.ownState);

        PictureKS pictKS = new PictureKS(blackBoard, dropperStates);

        SentimentKS sentKS = new SentimentKS(blackBoard, dropperStates);

        RemoveCompKS remCompKS = new RemoveCompKS(blackBoard, dropperStates);

        Thread t1 = new Thread(profKS);
        Thread t2 = new Thread(outKS);
        Thread t3 = new Thread(propKS);
        Thread t4 = new Thread(buyerKS);
        Thread t5 = new Thread(pictKS);
        Thread t6 = new Thread(sentKS);
        Thread t7 = new Thread(remCompKS);

        blackBoard.add(m1);
        blackBoard.add(m2);
        blackBoard.add(m3);
        blackBoard.add(m4);
        blackBoard.add(m5);
        blackBoard.add(m6);
        blackBoard.add(m7);
        blackBoard.add(m8);
        blackBoard.add(m9);
        blackBoard.add(m10);
        blackBoard.add(m11);
        blackBoard.add(m12);
        blackBoard.add(m13);
        blackBoard.add(m14);
        blackBoard.add(m15);
        blackBoard.add(m16);
        blackBoard.add(m17);
        blackBoard.add(m18);
        blackBoard.add(m19);
        blackBoard.add(m20);
        blackBoard.add(m21);
        blackBoard.add(m23);
        blackBoard.add(m24);
        blackBoard.add(m25);
        blackBoard.add(m26);
        blackBoard.add(m27);
        blackBoard.add(m28);
        blackBoard.add(m29);
        blackBoard.add(m30);
        blackBoard.add(m31);
        blackBoard.add(m32);
        blackBoard.add(m33);
        blackBoard.add(m34);
        blackBoard.add(m35);
        blackBoard.add(m36);
        blackBoard.add(m37);
        blackBoard.add(m38);
        blackBoard.add(m39);
        blackBoard.add(m40);
        

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        
    }
}
