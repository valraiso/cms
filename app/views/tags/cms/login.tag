${ plugins.cms.Tag.login() }
<div id="cms_user">
    <img src="/public/images/cadenas.png" alt="cms_login" />
    <form method="post" action="@{cms.CmsController.auth}">
        <table>
            <tr>
                <td><label for="cms_email">E-mail</label></td>
                <td><input id="cms_email" type="text" name="cms_email" /></td>
            </tr>
            <tr>
                <td><label for="cms_password">Password</label></td>
                <td><input id="cms_password" type="password" name="cms_password" /></td>
            </tr>
            <tr>
                <td colspan="2" style="text-align:right;padding-top:7px;">
                    <button type="submit" class="cms_action">Connexion</button>
                </td>
            </tr>
        </table>
    </form>
</div>
