import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.Random;

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

        for (Review review : blackBoard) 
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
                    break;
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
                        break;
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
            // System.out.println("Killed review");
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
            // System.out.println("passed review");
            blackBoard.put(review);
            this.review = null;
            try 
            {
                Thread.sleep(rnd.nextInt(800)+200);
       
            } catch (Exception e) 
            {
                e.printStackTrace();
            }
            return;
            

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

   OutputKS(ArrayBlockingQueue<Review> blackBoard, List<StateKS> dropperStates)
   {
       super(blackBoard, dropperStates);
       this.ownState = new OutputState();
   }


    @Override
    void execute() 
    {
        System.out.println(review.toString());

        review = null;
        
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
        
    }
}