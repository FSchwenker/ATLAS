function [ outTracks, outAudio, outVideo, outLabels, outScalars, outVectors ] = ALDemo( project, tracks, audio, video, labels, scalars, vectors )
%ALDEMO Summary of this function goes here
%   Detailed explanation goes here
[project, tracks, audio, video, labels, scalars, vectors] = atlasOut2Matlab(project, tracks, audio, video, labels, scalars, vectors);


load('PureGMMAL10.mat');


[~,~,name,~] = project.Attributes.Value;
if name == 'Proband-011'
    pIndex = 1;
else
    pIndex = 2;
end
database = ALresultsPD(pIndex).results;

itteration = str2double(labels{1}.Atrributes(2).Value) + 1;
labels{1}.Atrributes(2).Value = num2str(itteration);

if itteration>1
    subDB = sortrows(database(1:(itteration-1)*10,:),2);

    blockStartTime = subDB(1,2);
    blockEndTime = subDB(1,3);
    blockLabel = subDB(1,4);

    compactDB = [];
    compactDBIdx = 1;
    for i=2:size(subDB,1)
        if blockLabel==subDB(i,4)
            if blockEndTime<subDB(i,2)+1
                %end of block due time gap
                compactDB(compactDBIdx,1)=subDB(i-1,1);
                compactDB(compactDBIdx,2)=blockStartTime;
                compactDB(compactDBIdx,3)=subDB(i-1,3);
                compactDB(compactDBIdx,4)=blockLabel;
                blockStartTime = subDB(i,2);
                blockEndTime = subDB(i,3);
                blockLabel = subDB(i,4);
                compactDBIdx = compactDBIdx+1;
            else
                %do nothing, go on searching for end of block
                blockEndTime = subDB(i,3);
            end
        else
            if blockEndTime<subDB(i,2)+1
                %end of block due label change and time gap
                compactDB(compactDBIdx,1)=subDB(i-1,1);
                compactDB(compactDBIdx,2)=blockStartTime;
                compactDB(compactDBIdx,3)=subDB(i-1,3);
                compactDB(compactDBIdx,4)=blockLabel;
                blockStartTime = subDB(i,2);
                blockEndTime = subDB(i,3);
                blockLabel = subDB(i,4);
                compactDBIdx = compactDBIdx+1;
            else
                %end of block due label change
                compactDB(compactDBIdx,1)=subDB(i,1);
                compactDB(compactDBIdx,2)=blockStartTime;
                compactDB(compactDBIdx,3)=subDB(i,3)-1;
                compactDB(compactDBIdx,4)=blockLabel;
                blockStartTime = subDB(i,2);
                blockEndTime = subDB(i,3);
                blockLabel = subDB(i,4);
                compactDBIdx = compactDBIdx+1;
            end
        end
    end
    %what about the last one??
    i=i+1;
    compactDB(compactDBIdx,1)=subDB(i-1,1);
    compactDB(compactDBIdx,2)=blockStartTime;
    compactDB(compactDBIdx,3)=subDB(i-1,3);
    compactDB(compactDBIdx,4)=blockLabel;


    for labelIdx=1:compactDBIdx
        labels{1}.labels(labelIdx).startTime = compactDB(labelIdx,2);
        labels{1}.labels(labelIdx).endTime = compactDB(labelIdx,3);
        labels{1}.labels(labelIdx).timeStamp = 100000;
        labels{1}.labels(labelIdx).value = 1;
        labels{1}.labels(labelIdx).comment = '';
        labels{1}.labels(labelIdx).type = 'AUTOMATIC';
        labels{1}.labels(labelIdx).text = '';
        if compactDB(labelIdx,4)==-1
            labels{1}.labels(labelIdx).classentity = 'event';
        else
            labels{1}.labels(labelIdx).classentity = 'normal';
        end
        labels{1}.labels(labelIdx).showAsFlag = 0;
        labels{1}.labels(labelIdx).samplePoints(1).t=0;
        labels{1}.labels(labelIdx).samplePoints(2).t=(labels{1}.labels(labelIdx).endTime-labels{1}.labels(labelIdx).startTime)/2;
        labels{1}.labels(labelIdx).samplePoints(3).t=labels{1}.labels(labelIdx).endTime;
        labels{1}.labels(labelIdx).samplePoints(1).y=0.5;
        labels{1}.labels(labelIdx).samplePoints(2).y=0.5;
        labels{1}.labels(labelIdx).samplePoints(3).y=0.5;
    end
else
    labelIdx = 1;
end

for i=(itteration-1)*10+1:itteration*10
    labels{1}.labels(labelIdx).startTime = database(i,2);
    labels{1}.labels(labelIdx).endTime = database(i,2)+1;%database(i,3);
    labels{1}.labels(labelIdx).timeStamp = 100000;
    labels{1}.labels(labelIdx).value = 1;
    labels{1}.labels(labelIdx).comment = '';
    labels{1}.labels(labelIdx).type = 'AUTOMATIC';
    labels{1}.labels(labelIdx).text = '';
%    if database(i,4)==-1
%        labels{1}.labels(i).classentity = 'event';
%    else
%        labels{1}.labels(i).classentity = 'normal';
%    end
    labels{1}.labels(labelIdx).classentity = 'none';
    labels{1}.labels(labelIdx).showAsFlag = 1;
    labels{1}.labels(labelIdx).samplePoints(1).t=0;
    labels{1}.labels(labelIdx).samplePoints(2).t=(labels{1}.labels(labelIdx).endTime-labels{1}.labels(labelIdx).startTime)/2;
    labels{1}.labels(labelIdx).samplePoints(3).t=labels{1}.labels(labelIdx).endTime;
    labels{1}.labels(labelIdx).samplePoints(1).y=0.5;
    labels{1}.labels(labelIdx).samplePoints(2).y=0.5;
    labels{1}.labels(labelIdx).samplePoints(3).y=0.5;
    labelIdx=labelIdx+1;
end

[outTracks, outAudio, outVideo, outLabels, outScalars, outVectors] = matlab2AtlasIn(tracks, audio, video, labels, scalars, vectors);

end



