function [ project ] = atlasProjectXML2Struct( xmlString )
    %ATLASPROJECTXML2STRUCT Summary of this function goes here
    %   Detailed explanation goes here

    xmlwrite('tmp.xml', xmlReadString(xmlString));

    project = parseChildNodes(xmlread('tmp.xml'));
    project = rmfield(project,'Name');
    project = rmfield(project,'Data');
    project.Attributes(2).Value = str2num(project.Attributes(2).Value);
    project.Attributes(4).Value = str2num(project.Attributes(4).Value);
    project.LabelClasses = project.Children(2).Children;
    project.LabelTracks = project.Children(4).Children;
    project.DataTracks = project.Children(6).Children;
    project.MediaTracks = project.Children(8).Children;
    project = rmfield(project,'Children');
   
    
    t=1;
    for i=1:size(project.LabelClasses,2)
        if(strcmp(project.LabelClasses(i).Name, 'lclass'))
            tmp(t).Class = project.LabelClasses(i).Attributes.Value;
            tmp(t).File = project.LabelClasses(i).Children.Data;
            t=t+1;
        end
    end
    project.LabelClasses = tmp;
    
    clear tmp;
    t=1;
    for i=1:size(project.LabelTracks,2)
        if(strcmp(project.LabelTracks(i).Name, 'ltrack'))
            tmp(t).Attributes = project.LabelTracks(i).Attributes;
            tmp(t).File = project.LabelTracks(i).Children.Data;
            t=t+1;
        end
    end
    if exist('tmp','var') 
        project.LabelTracks = tmp;
    else
        project.LabelTracks = [];
    end

    clear tmp;
    t=1;
    for i=1:size(project.DataTracks(2).Children,2)
        if(strcmp(project.DataTracks(2).Children(i).Name, 'scatrack'))
            tmp(t).Attributes =project.DataTracks(2).Children(i).Attributes;
            tmp(t).File = project.DataTracks(2).Children(i).Children.Data;
            t=t+1;
        end
    end
    if exist('tmp','var') 
        project.ScalarTracks = tmp;
    else
        project.ScalarTracks = [];
    end
    
    clear tmp;
    t=1;
    for i=1:size(project.DataTracks(4).Children,2)
        if(strcmp(project.DataTracks(4).Children(i).Name, 'vectrack'))
            tmp(t).Attributes =project.DataTracks(4).Children(i).Attributes;
            tmp(t).File = project.DataTracks(4).Children(i).Children.Data;
            t=t+1;
        end
    end
    if exist('tmp','var') 
        project.VectorTracks = tmp;
    else
        project.VectorTracks = [];
    end
    project = rmfield(project,'DataTracks');

    clear tmp;
    t=1;
    for i=1:size(project.MediaTracks(2).Children,2)
        if(strcmp(project.MediaTracks(2).Children(i).Name, 'atrack'))
            tmp(t).Attributes =project.MediaTracks(2).Children(i).Attributes;
            tmp(t).File = project.MediaTracks(2).Children(i).Children.Data;
            t=t+1;
        end
    end
    if exist('tmp','var') 
        project.AudioTracks = tmp;
    else
        project.AudioTracks = [];
    end

    clear tmp;
    t=1;
    for i=1:size(project.MediaTracks(4).Children,2)
        if(strcmp(project.MediaTracks(4).Children(i).Name, 'vtrack'))
            tmp(t).Attributes =project.MediaTracks(4).Children(i).Attributes;
            tmp(t).File = project.MediaTracks(4).Children(i).Children.Data;
            t=t+1;
        end
    end
    if exist('tmp','var')
        project.VideoTracks = tmp;
    else
        project.VideoTracks = [];
    end
    project = rmfield(project,'MediaTracks');
    
    
end
    