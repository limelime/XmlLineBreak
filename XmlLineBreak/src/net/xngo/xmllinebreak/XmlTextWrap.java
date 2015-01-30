package net.xngo.xmllinebreak;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.util.Vector;
import java.util.Enumeration;

public class XmlTextWrap
{

    public static void main(String args[])
    {
        if(args.length != 4)
        {
            System.out.println("Parameter(s) missing.");
            System.out.println("Usage: <jarFilename>.jar <file_path> <element_name> <linebreak_length> <linebreak>");
            System.out.println("Example: jarFilename.jar c:\\input.xml div 15 \\n");
            System.exit(0);
        }
        
        int arg = 0;
        String filepath = args[arg++];
        String element  = args[arg++];
        int length      = Integer.parseInt(args[arg++]);
        String linebreak= args[arg++];

        try
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            NodeList transitions = doc.getElementsByTagName(element);
            for (int i = 0; i < transitions.getLength(); i++)
            {
                int childNodeNum = transitions.item(i).getChildNodes()
                        .getLength();
                if (childNodeNum == 0)
                {
                    // Do nothing.
                    // System.out.println(childNodeNum);
                } else if (childNodeNum == 1)
                {
                    String text = transitions.item(i).getFirstChild()
                                                        .getNodeValue().trim();
//System.out.println(i+":"+text);                
                    transitions
                            .item(i)
                            .getFirstChild()
                            .setTextContent(escape(replaceSpecialChars(wrapTextLine(text, length, linebreak))));
//System.out.println(i+":"+wrapTextLine(text, length, linebreak));                    
                } else
                {
                    System.out
                            .println("<transition> has other node type than text type.");
                }
            }
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

/*            
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            transformer.transform(source, xmlOutput);
            System.out.println(xmlOutput.getWriter().toString());

*/
        } catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
        } catch (TransformerException tfe)
        {
            tfe.printStackTrace();
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        } catch (SAXException sae)
        {
            sae.printStackTrace();
        }

    }
    static String escape(String str)
    {
        return str.replaceAll("\"", "\\\\\"");
    }
    
    static String replaceSpecialChars(String str)
    {
        String[] specialChars            = {"\\|"}; // Pipe is special character in regex. Hence, use escape character.
        String[] specialCharsReplacement = {"\\\\n----\\\\n"};
        for(int i=0; i<specialChars.length; i++)
        {
            str = str.replaceAll(specialChars[i], specialCharsReplacement[i]);
        }
        
        return str;
        
    }
    static String wrapTextLine(String str, int len, String replace)
    {
        String s = "";
        String[] text = wrapText(str, len);
        if( text.length > 1 )
        {
            for (int i = 0; i < text.length-1; i++)
            {
                s += text[i].trim() + replace;
            }
            s += text[text.length-1].trim();
        }
        else
            return str;
        
        return s;
    }

    static String[] wrapText(String text, int len)
    {
        // return empty array for null text
        if (text == null)
            return new String[] {};

        // return text if len is zero or less
        if (len <= 0)
            return new String[] { text };

        // return text if less than length
        if (text.length() <= len)
            return new String[] { text };

        char[] chars = text.toCharArray();
        Vector lines = new Vector();
        StringBuffer line = new StringBuffer();
        StringBuffer word = new StringBuffer();

        for (int i = 0; i < chars.length; i++)
        {
            word.append(chars[i]);

            if (chars[i] == ' ')
            {
                if ((line.length() + word.length()) > len)
                {
                    lines.add(line.toString());
                    line.delete(0, line.length());
                }

                line.append(word);
                word.delete(0, word.length());
            }
        }

        // handle any extra chars in current word
        if (word.length() > 0)
        {
            if ((line.length() + word.length()) > len)
            {
                lines.add(line.toString());
                line.delete(0, line.length());
            }
            line.append(word);
        }

        // handle extra line
        if (line.length() > 0)
        {
            lines.add(line.toString());
        }

        String[] ret = new String[lines.size()];
        int c = 0; // counter
        for (Enumeration e = lines.elements(); e.hasMoreElements(); c++)
        {
            ret[c] = (String) e.nextElement();
        }

        return ret;
    }
}