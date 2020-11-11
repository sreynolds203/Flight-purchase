

public class Attachment
{
	//class for the attachments contents
	
	//variables for the attachments class
    private String name;

    private byte[] contents;

    
    //getter and setter methods of the variables
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public byte[] getContents()
    {
        return contents;
    }

    public void setContents(byte[] contents)
    {
        this.contents = contents;
    }
}
