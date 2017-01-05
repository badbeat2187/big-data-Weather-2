import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

/**
 *
 * @author Sahul P
 * Nikhil S
 */
public class MaxRainEachStation 
{
    public static class MaxRainEachStationMapper extends MapReduceBase implements
        Mapper<LongWritable, Text, Text, DoubleWritable> 
    {
	public void map(LongWritable key, Text value,OutputCollector<Text, 
                DoubleWritable> output, Reporter reporter) throws IOException 
	{
            String line = value.toString();
            String station = line.substring(0, 6);
            double precipitation = 0;
            String precipitationValueRead = line.substring(118, 123).trim();
            
            // Ignoring the first value as its the heading
            if(!precipitationValueRead.equals("") && 
                    !precipitationValueRead.equals("PRCP"))
            {
                    precipitation = Double.parseDouble(precipitationValueRead);
                    if (precipitation != 99.99) 
                    {
                            output.collect(new Text(station), 
                                    new DoubleWritable(precipitation));
                    }
            }
	}
    }
    
    public static class MaxRainEachStationReducer extends MapReduceBase implements
                            Reducer<Text, DoubleWritable, Text, DoubleWritable> 
    {
        public void reduce(Text key, Iterator<DoubleWritable> values, 
                OutputCollector<Text, DoubleWritable> output, 
                Reporter reporter) throws IOException 
        {
            double maxPrecipitation = 0; 
            while (values.hasNext()) 
            {
                Double currentValue = values.next().get();
                if(currentValue>maxPrecipitation)
                {
                    maxPrecipitation = currentValue;
                }            
            }

            output.collect(key, new DoubleWritable(maxPrecipitation)); 
        }
    }
    
    public static void main(String[] args) throws IOException 
    {      
        JobConf jobConfiguration = new JobConf(MaxRainEachStation.class);
        jobConfiguration.setJobName("Finding maximum rain each station"
                + "for all years");
        
        FileInputFormat.addInputPath(jobConfiguration, new Path(args[0]));
        FileOutputFormat.setOutputPath(jobConfiguration, new Path(args[1]));
        
        jobConfiguration.setMapperClass(MaxRainEachStationMapper.class); 
        jobConfiguration.setReducerClass(MaxRainEachStationReducer.class);
        jobConfiguration.setOutputKeyClass(Text.class); 
        jobConfiguration.setOutputValueClass(DoubleWritable.class);
        
        JobClient.runJob(jobConfiguration); 
    }
}
