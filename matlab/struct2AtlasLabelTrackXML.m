function [ xmlString ] = struct2AtlasLabelTrackXML( labelTrack )

xml = com.mathworks.xml.XMLUtils.createDocument('LabelTrack');

root = xml.getDocumentElement;
root.setAttribute('classname',labelTrack.Atrributes(1).Value);
root.setAttribute('externalchange',labelTrack.Atrributes(2).Value);
root.setAttribute('interpolationType',labelTrack.Atrributes(3).Value);
root.setAttribute('isContinuous',labelTrack.Atrributes(4).Value);
root.setAttribute('name',labelTrack.Atrributes(5).Value);

if  isfield(labelTrack,'labels')
    numOfLabels = size(labelTrack.labels,2);
else
    numOfLabels = 0;
end

for i=1:numOfLabels
    label = xml.createElement('label');
    
    startTime=xml.createElement('starttime');
    startTime.appendChild(xml.createTextNode(num2str(labelTrack.labels(i).startTime)));
    label.appendChild(startTime);
    endTime=xml.createElement('endtime');
    endTime.appendChild(xml.createTextNode(num2str(labelTrack.labels(i).endTime)));
    label.appendChild(endTime);
    timestamp=xml.createElement('timestamp');
    timestamp.appendChild(xml.createTextNode(num2str(labelTrack.labels(i).timeStamp)));
    label.appendChild(timestamp);
    value=xml.createElement('value');
    value.appendChild(xml.createTextNode(num2str(labelTrack.labels(i).value)));
    label.appendChild(value);
    comment=xml.createElement('comment');
    comment.appendChild(xml.createTextNode(labelTrack.labels(i).comment));
    label.appendChild(comment);
    type=xml.createElement('type');
    type.appendChild(xml.createTextNode(labelTrack.labels(i).type));
    label.appendChild(type);
    text=xml.createElement('text');
    text.appendChild(xml.createTextNode(labelTrack.labels(i).text));
    label.appendChild(text);
    classentity=xml.createElement('classentity');
    classentity.appendChild(xml.createTextNode(labelTrack.labels(i).classentity));
    label.appendChild(classentity);
    showasflag=xml.createElement('showAsFlag');
    if labelTrack.labels(i).showAsFlag 
        showasflag.appendChild(xml.createTextNode('true'));
    else
        showasflag.appendChild(xml.createTextNode('false'));
    end
    label.appendChild(showasflag);
    
    continuousSamplingPoints = xml.createElement('continuousSamplingPoints');
    
    numOfSamplePoints = size(labelTrack.labels(i).samplePoints,2);
    for j=1:numOfSamplePoints
        samplePoint=xml.createElement('samplePoint');
        t = xml.createAttribute('t');
        t.setNodeValue(num2str(labelTrack.labels(i).samplePoints(j).t));
        samplePoint.setAttributeNode(t);
        y = xml.createAttribute('y');
        y.setNodeValue(num2str(labelTrack.labels(i).samplePoints(j).y));
        samplePoint.setAttributeNode(y);
        continuousSamplingPoints.appendChild(samplePoint);
    end
    label.appendChild(continuousSamplingPoints);


    
    
    
    root.appendChild(label);
end
xmlwrite('tmp.xml',xml);
xmlString = fileread('tmp.xml');
delete('tmp.xml');
end