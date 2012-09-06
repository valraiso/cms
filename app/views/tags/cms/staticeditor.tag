%{
    ( _arg ) &&  ( _code = _arg);

    if(! _code) {
        throw new play.exceptions.TagInternalException("code attribute cannot be empty for cms.staticeditor tag");
    }

}%
${ plugins.cms.Tag.staticeditor(_code) }