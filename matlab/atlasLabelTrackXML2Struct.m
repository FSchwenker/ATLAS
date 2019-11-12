function [ track ] = atlasLabelTrackXML2Struct( xmlString )
    %ATLASPROJECTXML2STRUCT Summary of this function goes here
    %   Detailed explanation goes here

    xmlwrite('tmp.xml', xmlReadString(xmlString));
    xml = parseChildNodes(xmlread('tmp.xml'));
    delete('tmp.xml');
    
    track.Atrributes = xml.Attributes;
    
    l=1;
    for i=1:size(xml.Children,2)
        if(strcmp(xml.Children(i).Name, 'label'))
            labels(l).startTime=str2double(xml.Children(i).Children(2).Children.Data);
            labels(l).endTime  =str2double(xml.Children(i).Children(4).Children.Data);
            labels(l).timeStamp=str2double(xml.Children(i).Children(6).Children.Data);
            labels(l).value    =str2double(xml.Children(i).Children(8).Children.Data);
            if size(xml.Children(i).Children(10).Children,1)~=0
                labels(l).comment=xml.Children(i).Children(10).Children.Data;
            else
                labels(l).comment='';
            end
            if size(xml.Children(i).Children(12).Children,1)~=0
                labels(l).type=xml.Children(i).Children(12).Children.Data;
            end
            if size(xml.Children(i).Children(14).Children,1)~=0
                labels(l).text=xml.Children(i).Children(14).Children.Data;
            else
                labels(l).text='';
            end
            if size(xml.Children(i).Children(16).Children,1)~=0
                labels(l).classentity=xml.Children(i).Children(16).Children.Data;
            else
                labels(l).classentity='';
            end
            if size(xml.Children(i).Children(18).Children,1)~=0
                labels(l).showAsFlag=str2num(xml.Children(i).Children(18).Children.Data);
            end
            if size(xml.Children(i).Children(20).Children,1)~=0
                s=1;
                for j=1:size(xml.Children(i).Children(20).Children,2)
                    if(strcmp(xml.Children(i).Children(20).Children(j).Name,'samplePoint'))
                        samplePoints(s).t=xml.Children(i).Children(20).Children(j).Attributes(1).Value;
                        samplePoints(s).y=xml.Children(i).Children(20).Children(j).Attributes(2).Value;
                        s=s+1;
                    end
                end
                labels(l).samplePoints = samplePoints;
            end
            
            l=l+1;
        track.labels = labels;
        end
    end
end
   
    
    
    
    

    