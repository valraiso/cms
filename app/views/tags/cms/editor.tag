%{
    ( _arg ) &&  ( _code = _arg);
    
    if(! _code) {
        throw new play.exceptions.TagInternalException("code attribute cannot be empty for cms.editor tag");
    }

}%
${ plugins.cms.Tag.editor(_code) }